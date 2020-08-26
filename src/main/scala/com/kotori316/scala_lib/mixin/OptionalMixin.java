package com.kotori316.scala_lib.mixin;

import java.util.Optional;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.common.util.NonNullPredicate;
import net.minecraftforge.common.util.NonNullSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kotori316.scala_lib.util.LazySupplierWrapper;

/**
 * Mixin Object for {@link LazyOptional}.
 * Works when the supplier is instance of {@link LazySupplierWrapper}, the wrapper of {@link cats.data.OptionT} and {@link cats.Eval}.
 *
 * @param <T> the same as the parameter {@link LazyOptional} has.
 */
@SuppressWarnings("unchecked")
@Mixin(LazyOptional.class)
public abstract class OptionalMixin<T> {
    private boolean kotori_scala_LazyOptional_wrapping = false;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    public void initMixin(NonNullSupplier<?> instanceSupplier, CallbackInfo ci) {
        if (instanceSupplier instanceof LazySupplierWrapper<?>) {
            kotori_scala_LazyOptional_wrapping = true;
        }
    }

    @Inject(method = "getValue", at = @At("HEAD"), remap = false)
    private void getValueMixin(CallbackInfoReturnable<T> cir) {
        if (kotori_scala_LazyOptional_wrapping) {
            throw new IllegalStateException("getValue is accessed from mixin modified optional. Did API change?");
        }
    }

    @Inject(method = "isPresent", at = @At("HEAD"), cancellable = true, remap = false)
    public void isPresentMixin(CallbackInfoReturnable<Boolean> cir) {
        if (kotori_scala_LazyOptional_wrapping && isValid) {
            boolean present = ((LazySupplierWrapper<T>) supplier).isPresent();
            if (!present) {
                invalidate();
            }
            cir.setReturnValue(present);
        }
    }

    @Inject(method = "ifPresent", at = @At("HEAD"), cancellable = true, remap = false)
    public void ifPresentMixin(NonNullConsumer<? super T> consumer, CallbackInfo ci) {
        if (kotori_scala_LazyOptional_wrapping) {
            if (isValid)
                ((LazySupplierWrapper<T>) supplier).ifPresent(consumer, this::invalidate);
            ci.cancel();
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "map", at = @At("HEAD"), cancellable = true, remap = false)
    public <U> void mapMixin(NonNullFunction<? super T, ? extends U> mapper, CallbackInfoReturnable<Optional<U>> cir) {
        if (kotori_scala_LazyOptional_wrapping) {
            Optional<U> r;
            if (isValid) {
                LazySupplierWrapper<U> wrapper = ((LazySupplierWrapper<T>) supplier).map(mapper);
                r = wrapper.getAsJava();
            } else {
                r = Optional.empty();
            }
            cir.setReturnValue(r);
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "lazyMap", at = @At("HEAD"), cancellable = true, remap = false)
    public <U> void lazyMapMixin(NonNullFunction<? super T, ? extends U> mapper, CallbackInfoReturnable<LazyOptional<U>> cir) {
        if (kotori_scala_LazyOptional_wrapping) {
            LazyOptional<U> r;
            if (isValid) {
                LazySupplierWrapper<U> wrapper = ((LazySupplierWrapper<T>) supplier).map(mapper);
                r = LazyOptional.of(wrapper);
            } else {
                r = LazyOptional.empty();
            }
            cir.setReturnValue(r);
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "filter", at = @At("HEAD"), cancellable = true, remap = false)
    public void filterMixin(NonNullPredicate<? super T> predicate, CallbackInfoReturnable<Optional<T>> cir) {
        if (kotori_scala_LazyOptional_wrapping) {
            if (isValid) {
                LazySupplierWrapper<T> wrapper = ((LazySupplierWrapper<T>) supplier).filter(predicate);
                cir.setReturnValue(wrapper.getAsJava());
            } else {
                cir.setReturnValue(Optional.empty());
            }
        }
    }

    @Inject(method = "resolve", at = @At("HEAD"), cancellable = true, remap = false)
    public void resolveMixin(CallbackInfoReturnable<Optional<T>> cir) {
        if (kotori_scala_LazyOptional_wrapping && isValid) {
            cir.setReturnValue(((LazySupplierWrapper<T>) supplier).getAsJava());
        }
    }

    @Inject(method = "orElse", at = @At("HEAD"), cancellable = true, remap = false)
    public void orElseMixin(T other, CallbackInfoReturnable<T> cir) {
        if (kotori_scala_LazyOptional_wrapping) {
            T value;
            if (isValid)
                value = ((LazySupplierWrapper<T>) supplier).orElse(other, this::invalidate);
            else value = other;
            cir.setReturnValue(value);
        }
    }

    @Inject(method = "orElseGet", at = @At("HEAD"), cancellable = true, remap = false)
    public void orElseGetMixin(NonNullSupplier<? extends T> other, CallbackInfoReturnable<T> cir) {
        if (kotori_scala_LazyOptional_wrapping) {
            T value;
            if (isValid)
                value = ((LazySupplierWrapper<T>) supplier).orElse(other, this::invalidate);
            else
                value = other.get();
            cir.setReturnValue(value);
        }
    }

    @Inject(method = "orElseThrow", at = @At("HEAD"), cancellable = true, remap = false)
    public <X extends Throwable> void orElseThrowMixin(NonNullSupplier<? extends X> exceptionSupplier, CallbackInfoReturnable<T> cir) throws X {
        if (kotori_scala_LazyOptional_wrapping) {
            if (isValid)
                cir.setReturnValue(((LazySupplierWrapper<T>) supplier).orThrow(exceptionSupplier, this::invalidate));
            else throw exceptionSupplier.get();
        }
    }

    @Shadow(remap = false)
    public abstract void invalidate();

    @Shadow(remap = false)
    private boolean isValid;
    @Final
    @SuppressWarnings("unused")
    private NonNullSupplier<?> supplier;
}
