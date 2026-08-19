package axthrix.world.util.ui;

import arc.Core;
import arc.Events;
import arc.audio.Sound;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.Button;
import arc.scene.ui.TextButton;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.Cell;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.ui.dialogs.BaseDialog;

public class AnimatedWelcomeDialog {
    public String titleKey;
    public String messageKey;
    public String spritePrefix;
    public Sound cutsceneSound;

    public int totalFrames;
    public float chanceToTrigger;
    public float width, height;

    /// Total duration of the audio track in seconds for millisecond sync calculations
    public float targetAudioDurationSeconds = 40.26f;
    public boolean showSkipButton = true;
    public boolean loopAnimation = false;
    public float delayBetweenLoops = 60f;
    public float cutsceneVolume = 1.0f;
    public boolean isMusicMode = false;

    private static boolean globalMusicModeActive = false;
    private static Sound activeGlobalMusicSound = null;
    private static long globalMusicStartTimeMillis = 0L;
    private static float globalMusicExpectedDurationSec = 0f;

    public AnimatedWelcomeDialog(String titleKey, String messageKey, String spritePrefix, Sound cutsceneSound, int totalFrames) {
        this.titleKey = titleKey;
        this.messageKey = messageKey;
        this.spritePrefix = spritePrefix;
        this.cutsceneSound = cutsceneSound;
        this.totalFrames = totalFrames;

        this.chanceToTrigger = 1.0f;
        this.width = 160f;
        this.height = 50f;
    }
    static {
        Events.run(EventType.Trigger.update, () -> {
            if (!globalMusicModeActive || activeGlobalMusicSound == null) return;

            long elapsed = Time.millis() - globalMusicStartTimeMillis;
            float totalExpected = globalMusicExpectedDurationSec * 1000f;

            if (elapsed >= totalExpected) {
                globalMusicModeActive = false;
                activeGlobalMusicSound.stop();
                activeGlobalMusicSound = null;

                if (Vars.control != null && Vars.control.sound != null) {
                    Vars.control.sound.playRandom();
                }
            }
        });
    }

    public void show() {
        BaseDialog dialog = new BaseDialog(Core.bundle.get(titleKey));
        dialog.cont.add(Core.bundle.get(messageKey)).row();

        boolean rareCheckPassed = Mathf.chance(chanceToTrigger);

        if (rareCheckPassed && isMusicMode) {
            TextureRegion staticIcon = Core.atlas.find("aj-" + spritePrefix + "-icon", Core.atlas.find("error"));
            dialog.cont.image(staticIcon).size(512f, 512f).pad(20f).row();

            Cell<TextButton> btnCell = dialog.cont.button(Core.bundle.get("button.aj-proceed"), () -> {});
            btnCell.size(width, height).padBottom(10f).row();
            Button proceedButton = btnCell.get();

            proceedButton.changed(() -> {
                proceedButton.setDisabled(true);

                if (Vars.control != null && Vars.control.sound != null) Vars.control.sound.stop();
                if (cutsceneSound != null) {
                    try { cutsceneSound.play(cutsceneVolume); } catch (Exception ignored) {}
                }

                globalMusicModeActive = true;
                activeGlobalMusicSound = cutsceneSound;
                globalMusicStartTimeMillis = Time.millis();
                globalMusicExpectedDurationSec = targetAudioDurationSeconds;

                Table controlsTable = new Table();
                controlsTable.left().defaults().left().pad(5f);

                controlsTable.button(Core.bundle.get("button.aj-continue"), dialog::hide).size(width, height);

                controlsTable.button(Core.bundle.get("button.aj-skip"), () -> {
                    globalMusicModeActive = false;
                    if (cutsceneSound != null) cutsceneSound.stop();
                    activeGlobalMusicSound = null;
                    if (Vars.control != null && Vars.control.sound != null) Vars.control.sound.playRandom();
                    dialog.hide();
                }).size(width, height);

                dialog.cont.add(controlsTable).padTop(10f).row();
            });
        } else if (rareCheckPassed && totalFrames > 0) {
            TextureRegionDrawable[] compiledDrawables = new TextureRegionDrawable[totalFrames];
            TextureRegion defaultIcon = Core.atlas.find("aj-" + spritePrefix + "-icon", Core.atlas.find("error"));

            for (int i = 0; i < totalFrames; i++) {
                TextureRegion frameRegion = Core.atlas.find("aj-" + spritePrefix + "-" + i, defaultIcon);
                compiledDrawables[i] = new TextureRegionDrawable(frameRegion);
            }

            Table videoTable = new Table();
            Cell<Image> imgCell = videoTable.image(defaultIcon).pad(10f);
            imgCell.size(512f, 512f);
            Image animationCell = imgCell.get();
            dialog.cont.add(videoTable).pad(15f).row();

            // FIX: Initialized cell array containing proper specific generic types
            @SuppressWarnings("unchecked")
            final Cell<TextButton>[] skipButtonCell = (Cell<TextButton>[]) new Cell[1];

            Cell<TextButton> btnCell = dialog.cont.button(Core.bundle.get("button.aj-proceed"), () -> {});
            btnCell.size(width, height).padBottom(10f).row();
            Button proceedButton = btnCell.get();

            Cell<TextButton> skipBtnCell = dialog.cont.button(Core.bundle.get("button.aj-skip"), () -> {});
            skipBtnCell.size(width, height).padBottom(10f).row();
            Button skipButton = skipBtnCell.get();

            skipButtonCell[0] = skipBtnCell;
            skipButtonCell[0].visible(false); // FIX: Toggled visibility correctly using method parameters instead of assignments

            skipButton.changed(() -> {
                if (cutsceneSound != null) cutsceneSound.stop();
                if (Vars.control != null && Vars.control.sound != null) Vars.control.sound.playRandom();
                videoTable.update(null);
                dialog.hide();
            });

            proceedButton.changed(() -> {
                proceedButton.setDisabled(true);

                if (showSkipButton && skipButtonCell[0] != null) {
                    skipButtonCell[0].visible(true);
                }

                if (Vars.control != null && Vars.control.sound != null) Vars.control.sound.stop();
                if (cutsceneSound != null) {
                    try { cutsceneSound.play(cutsceneVolume); } catch (Exception ignored) {}
                }

                final long[] audioStartTimeMillis = {Time.millis()};
                final int[] lastRenderedFrameIndex = {-1};
                final float[] loopDelayTimer = {0f};
                final boolean[] animationFinished = {false};

                videoTable.update(() -> {
                    // FIX: Appended index brackets to read array final wrappers perfectly inside the lambda update ticks
                    if (animationFinished[0]) return;

                    long currentDurationElapsed = Time.millis() - audioStartTimeMillis[0];
                    float totalExpectedDurationMillis = targetAudioDurationSeconds * 1000f;

                    float progressPercentage = currentDurationElapsed / totalExpectedDurationMillis;
                    int calculatedTargetFrame = (int)(progressPercentage * totalFrames);

                    if (calculatedTargetFrame >= totalFrames) {
                        if (loopAnimation) {
                            loopDelayTimer[0] += Time.delta;
                            if (loopDelayTimer[0] >= delayBetweenLoops) {
                                loopDelayTimer[0] = 0f;
                                audioStartTimeMillis[0] = Time.millis();
                                lastRenderedFrameIndex[0] = -1;
                                if (cutsceneSound != null) {
                                    try { cutsceneSound.play(cutsceneVolume); } catch (Exception ignored) {}
                                }
                            }
                        } else {
                            animationFinished[0] = true;
                            if (cutsceneSound != null) cutsceneSound.stop();
                            if (Vars.control != null && Vars.control.sound != null) {
                                Vars.control.sound.playRandom();
                            }
                            videoTable.update(null);
                            Time.runTask(5f, dialog::hide);
                        }
                        return;
                    }

                    if (calculatedTargetFrame != lastRenderedFrameIndex[0] && calculatedTargetFrame >= 0) {
                        lastRenderedFrameIndex[0] = calculatedTargetFrame;

                        if (Vars.control != null && Vars.control.sound != null) {
                            Vars.control.sound.stop();
                        }

                        videoTable.clear();
                        Cell<Image> freshImgCell = videoTable.image(compiledDrawables[calculatedTargetFrame]).pad(10f);
                        freshImgCell.size(512f, 512f);

                        videoTable.pack();
                        videoTable.invalidateHierarchy();
                    }
                });
            });
        } else {
            TextureRegion fallbackIcon = Core.atlas.find("aj-" + spritePrefix + "-icon", Core.atlas.find("aj-" + spritePrefix + "-0"));
            dialog.cont.image(fallbackIcon).pad(20f).row();
            dialog.cont.button(Core.bundle.get("button.aj-okay"), dialog::hide).size(100f, 50f);
        }

        dialog.show();
    }
}
