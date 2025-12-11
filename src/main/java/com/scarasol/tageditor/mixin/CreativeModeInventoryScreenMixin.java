package com.scarasol.tageditor.mixin;

import com.scarasol.tageditor.TagEditorMod;
import com.scarasol.tageditor.configuration.CommonConfig;
import com.scarasol.tageditor.util.TagHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * @author Scarasol
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {


    @Shadow
    @Nullable
    private EditBox searchBox;

    @Unique
    private String tagEdit$savedTagBoxText = null;

    @Unique
    private EditBox tagEdit$tagBox = null;

    @Unique
    private Button tagEdit$editButton = null;

    @Unique
    private boolean tagEdit$isEdit = false;

    @Unique
    private Component tagEdit$getButtonComponent() {
        return tagEdit$isEdit ? Component.translatable("gui.tag_editor.creative_mode_inventory.button.save") : Component.translatable("gui.tag_editor.creative_mode_inventory.button.edit");
    }

    @Override
    @Unique
    public void onClose() {
        TagHelper.resetBuffer();
        super.onClose();
    }

    public CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu p_98701_, Inventory p_98702_, Component p_98703_) {
        super(p_98701_, p_98702_, p_98703_);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void tagEdit$InitReturn(CallbackInfo ci) {
        if (CommonConfig.OPEN_EDIT.get() && Minecraft.getInstance().player.getPermissionLevel() >= 2) {
            int boxWidth = 100;
            int boxHeight = 15;
            int buttonHeight = 18;
            int gap = 6;

            int x = this.leftPos + (this.imageWidth - boxWidth - 50 - gap) / 2;
            int y = this.topPos + this.imageHeight + 35;

            this.tagEdit$tagBox = new EditBox(this.font, x, y, boxWidth, boxHeight, Component.literal("Filter"));
            this.tagEdit$tagBox.setValue("");
            this.tagEdit$tagBox.setMaxLength(128);
            this.tagEdit$tagBox.setVisible(true);
            this.tagEdit$tagBox.setCanLoseFocus(true);
            this.tagEdit$tagBox.setFilter(s -> s.matches("^[A-Za-z0-9_/.-]*(:[A-Za-z0-9_/.-]*)?$"));
            this.tagEdit$tagBox.setResponder(text -> {
                if (text == null) {
                    TagHelper.BUFFER_TAG = null;
                    return;
                }
                String value = text.trim().toLowerCase();
                if (value.isEmpty()) {
                    TagHelper.BUFFER_TAG = null;
                    return;
                }
                ResourceLocation rl = new ResourceLocation(value.toLowerCase());
                TagHelper.BUFFER_TAG = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, rl);
            });

            this.tagEdit$editButton = Button.builder(tagEdit$getButtonComponent(), btn -> {
                if (tagEdit$isEdit) {
                    TagHelper.saveTag();
                }
                tagEdit$isEdit = !tagEdit$isEdit;
                btn.setMessage(tagEdit$getButtonComponent());
            }).bounds(x + boxWidth + gap, y - 2, 50, buttonHeight).build();

            this.addRenderableWidget(this.tagEdit$tagBox);
            this.addRenderableWidget(this.tagEdit$editButton);
        }


    }

    @Inject(method = "resize", at = @At("HEAD"))
    private void tagEdit$resizeHead(Minecraft minecraft, int width, int height, CallbackInfo ci) {
        if (this.tagEdit$tagBox == null) {
            return;
        }
        this.tagEdit$savedTagBoxText = tagEdit$tagBox.getValue();
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void tagEdit$resizeTail(Minecraft minecraft, int width, int height, CallbackInfo ci) {
        if (this.tagEdit$tagBox == null) {
            return;
        }
        tagEdit$tagBox.setValue(this.tagEdit$savedTagBoxText);
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void tagEdit$CharTyped(char typedChar, int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (this.tagEdit$tagBox == null) {
            return;
        }
        if (!this.tagEdit$tagBox.isFocused()) {
            return;
        }

        boolean handled = this.tagEdit$tagBox.charTyped(typedChar, keyCode);
        if (handled) {

            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void tagEdit$KeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.tagEdit$tagBox == null) {
            return;
        }
        if (!this.tagEdit$tagBox.isFocused()) {
            return;
        }
        if (this.searchBox != null && this.tagEdit$tagBox == this.searchBox) {
            return;
        }

        boolean handled = this.tagEdit$tagBox.keyPressed(keyCode, scanCode, modifiers);
        if (handled) {
            cir.setReturnValue(true);
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.keyInventory.matches(keyCode, scanCode) || mc.options.keyChat.matches(keyCode, scanCode)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void tagEdit$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {

        if (button == 0 && tagEdit$isEdit && tagEdit$tagBox != null) {

            for (int i = 0; i < this.menu.slots.size(); i++) {
                var slot = this.menu.getSlot(i);
                double slotX = this.leftPos + slot.x;
                double slotY = this.topPos + slot.y;
                int slotSize = 16;
                if (mouseX >= slotX && mouseX <= slotX + slotSize && mouseY >= slotY && mouseY <= slotY + slotSize) {
                    if (!slot.hasItem()) {
                        break;
                    }

                    var stack = slot.getItem();
                    if (!stack.isEmpty()) {
                        TagHelper.changeItem(stack);
                    }

                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

}
