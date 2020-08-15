package com.kotori316.scala_lib.mixin;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.common.util.NonNullPredicate;
import net.minecraftforge.common.util.NonNullSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kotori316.scala_lib.util.LazySupplierWrapper;

@Mixin(LazyOptional.class)
public abstract class OptionalMixin<T> {
    private LazySupplierWrapper<T> kotori_scala_LazyOptional_wrapper;

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("TAIL"))
    public void initMixin(NonNullSupplier<?> instanceSupplier, CallbackInfo ci) {
        if (instanceSupplier instanceof LazySupplierWrapper<?>) {
            kotori_scala_LazyOptional_wrapper = (LazySupplierWrapper<T>) instanceSupplier;
        }
    }

    @Inject(method = "isPresent", at = @At("HEAD"), cancellable = true)
    public void isPresentMixin(CallbackInfoReturnable<Boolean> cir) {
        if (kotori_scala_LazyOptional_wrapper != null) {
            if (!isValid) {
                cir.setReturnValue(false);
                return;
            }
            boolean present = kotori_scala_LazyOptional_wrapper.isPresent();
            if (!present) {
                invalidate();
            }
            cir.setReturnValue(present);
        }
    }

    @Inject(method = "ifPresent", at = @At("HEAD"), cancellable = true)
    public void ifPresentMixin(NonNullConsumer<? super T> consumer, CallbackInfo ci) {
        if (kotori_scala_LazyOptional_wrapper != null) {
            if (isValid)
                kotori_scala_LazyOptional_wrapper.ifPresent(consumer, this::invalidate);
            ci.cancel();
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "map", at = @At("HEAD"), cancellable = true)
    public <U> void mapMixin(NonNullFunction<? super T, ? extends U> mapper, CallbackInfoReturnable<LazyOptional<U>> cir) {
        if (kotori_scala_LazyOptional_wrapper != null) {
            if (isValid) {
                LazySupplierWrapper<U> wrapper = kotori_scala_LazyOptional_wrapper.map(mapper);
                cir.setReturnValue(LazyOptional.of((NonNullSupplier<U>) wrapper));
            } else {
                cir.setReturnValue(LazyOptional.empty());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "filter", at = @At("HEAD"), cancellable = true)
    public void filterMixin(NonNullPredicate<? super T> predicate, CallbackInfoReturnable<LazyOptional<T>> cir) {
        if (kotori_scala_LazyOptional_wrapper != null) {
            if (isValid) {
                LazySupplierWrapper<T> wrapper = kotori_scala_LazyOptional_wrapper.filter(predicate);
                cir.setReturnValue(LazyOptional.of((NonNullSupplier<T>) wrapper));
            } else {
                cir.setReturnValue(LazyOptional.empty());
            }
        }
    }

    @Inject(method = "orElse", at = @At("HEAD"), cancellable = true)
    public void orElseMixin(T other, CallbackInfoReturnable<T> cir) {
        if (kotori_scala_LazyOptional_wrapper != null) {
            T value;
            if (isValid)
                value = kotori_scala_LazyOptional_wrapper.orElse(other, this::invalidate);
            else value = other;
            cir.setReturnValue(value);
        }
    }

    @Inject(method = "orElseGet", at = @At("HEAD"), cancellable = true)
    public void orElseGetMixin(NonNullSupplier<? extends T> other, CallbackInfoReturnable<T> cir) {
        if (kotori_scala_LazyOptional_wrapper != null) {
            T value;
            if (isValid)
                value = kotori_scala_LazyOptional_wrapper.orElse(other, this::invalidate);
            else
                value = other.get();
            cir.setReturnValue(value);
        }
    }

    @Inject(method = "orElseThrow", at = @At("HEAD"), cancellable = true)
    public <X extends Throwable> void orElseThrowMixin(NonNullSupplier<? extends X> exceptionSupplier, CallbackInfoReturnable<T> cir) throws X {
        if (kotori_scala_LazyOptional_wrapper != null) {
            if (isValid)
                cir.setReturnValue(kotori_scala_LazyOptional_wrapper.orThrow(exceptionSupplier, this::invalidate));
            else throw exceptionSupplier.get();
        }
    }

    @Shadow
    public abstract void invalidate();

    @Shadow
    private boolean isValid;
}
