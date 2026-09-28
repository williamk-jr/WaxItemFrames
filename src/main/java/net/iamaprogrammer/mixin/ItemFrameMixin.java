package net.iamaprogrammer.mixin;

import net.iamaprogrammer.WaxItemFrames;
import net.iamaprogrammer.util.WaxedItemFrameAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public class ItemFrameMixin implements WaxedItemFrameAccess {
	@Unique
	private boolean waxed;

	@Unique
	private ItemFrame frame() {
		return (ItemFrame) (Object) this;
	}

	/** 26.3: interact(Player, InteractionHand, Vec3) */
	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void waxItemFrames$interact(
			Player player,
			InteractionHand hand,
			Vec3 hit,
			CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack stack = player.getItemInHand(hand);
		ItemFrame frame = frame();

		if (player.level() instanceof ServerLevel && !stack.isEmpty() && player.isShiftKeyDown()) {
			if (!this.isWaxed() && stack.getItem() instanceof HoneycombItem) {
				this.setWaxed(true);
				stack.consume(1, player);
				frame.playSound(SoundEvents.HONEYCOMB_WAX_ON, 1.0f, 1.0f);
				player.level().levelEvent(null, 3003, frame.blockPosition(), 0);
				cir.setReturnValue(InteractionResult.SUCCESS);
				return;
			}
			if (this.isWaxed() && stack.is(ItemTags.AXES)) {
				this.setWaxed(false);
				stack.hurtAndBreak(1, player, hand);
				frame.playSound(SoundEvents.WAXED_SIGN_INTERACT_FAIL, 1.0f, 1.0f);
				cir.setReturnValue(InteractionResult.SUCCESS);
				return;
			}
		}

		if (this.isWaxed()) {
			frame.playSound(SoundEvents.WAXED_SIGN_INTERACT_FAIL, 1.0f, 1.0f);
			cir.setReturnValue(InteractionResult.PASS);
		}
	}

	@Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
	private void waxItemFrames$hurt(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (this.isWaxed()) {
			frame().playSound(SoundEvents.WAXED_SIGN_INTERACT_FAIL, 1.0f, 1.0f);
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "survives", at = @At("HEAD"), cancellable = true)
	private void waxItemFrames$survives(CallbackInfoReturnable<Boolean> cir) {
		if (WaxItemFrames.CONFIG != null
				&& WaxItemFrames.CONFIG.isItemFrameFixedWhenWaxed()
				&& this.isWaxed()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void waxItemFrames$write(ValueOutput output, CallbackInfo ci) {
		output.putBoolean("Waxed", this.isWaxed());
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void waxItemFrames$read(ValueInput input, CallbackInfo ci) {
		this.setWaxed(input.getBooleanOr("Waxed", false));
	}

	@Override
	public void setWaxed(boolean waxed) {
		this.waxed = waxed;
	}

	@Override
	public boolean isWaxed() {
		return this.waxed;
	}
}
