package net.barribob.boss.mob.utils.animation

import software.bernie.geckolib3.core.IAnimatable
import software.bernie.geckolib3.core.PlayState
import software.bernie.geckolib3.core.controller.AnimationController
import software.bernie.geckolib3.core.event.predicate.AnimationEvent

class AnimationPredicate<T : IAnimatable>(val predicate: (AnimationEvent<*>) -> PlayState) :
    AnimationController.IAnimationPredicate<T> {
    override fun test(p0: AnimationEvent<T>): PlayState = predicate(p0)
}