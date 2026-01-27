package cn.sweetberry.mcmod.vitalsignals.damage.context

interface DamageContextHolder {
    @Suppress("FunctionName") // for mixin compatibility
    fun fabric_vital_signals_getDamageContext(): DamageContext
}