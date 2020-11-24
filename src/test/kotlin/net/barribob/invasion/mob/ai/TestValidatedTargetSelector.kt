package net.barribob.invasion.mob.ai

import net.barribob.invasion.utils.IVelPos
import net.barribob.invasion.testing_utilities.StubVelPos
import net.barribob.maelstrom.general.random.IRandom
import net.barribob.maelstrom.static_utilities.RandomUtils
import net.barribob.maelstrom.static_utilities.VecUtils
import net.minecraft.util.math.Vec3d
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class TestValidatedTargetSelector {

    @Test
    fun getTarget_AdjustsTarget_WhenValid() {
        val targetSelector = createTargetSelector(StubVelPos(), { true }, { RandomUtils.randVec() }, { 1.0 })

        val target = targetSelector.getTarget()
        val target2 = targetSelector.getTarget()

        Assertions.assertNotEquals(target, target2)
    }

    @Test
    fun getTarget_Reverses_WhenInvalid() {
        val direction = VecUtils.xAxis
        val targetSelector = createTargetSelector(StubVelPos(), { false }, { direction }, { 1.0 })

        val target = targetSelector.getTarget()
        val expected = direction.negate()
        val error = 0.0001

        assertAll(
            { assertEquals(expected.x, target.x, error) },
            { assertEquals(expected.y, target.y, error) },
            { assertEquals(expected.z, target.z, error) })
    }

    @Test
    fun getTarget_AdjustsForPosition() {
        val targetSelector =
            createTargetSelector(StubVelPos(position = VecUtils.unit), { true }, { VecUtils.xAxis }, { 0.0 })

        val target = targetSelector.getTarget()
        val expected = Vec3d(2.0, 1.0, 1.0)
        val error = 0.0001

        assertAll(
            { assertEquals(expected.x, target.x, error) },
            { assertEquals(expected.y, target.y, error) },
            { assertEquals(expected.z, target.z, error) })
    }

    private fun createTargetSelector(
        velPos: IVelPos,
        validator: (Vec3d) -> Boolean,
        randomVec: () -> Vec3d,
        randomDouble: () -> Double
    ) = ValidatedTargetSelector(velPos, validator, StubRandom(randomVec, randomDouble))

    private class StubRandom(
        val randomVec: () -> Vec3d,
        val randomDouble: () -> Double
    ) : IRandom {
        override fun getDouble(): Double = randomDouble()
        override fun getVector(): Vec3d = randomVec()
    }
}