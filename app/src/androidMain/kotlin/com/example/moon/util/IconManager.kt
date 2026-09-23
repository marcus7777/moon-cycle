package com.example.moon.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.moon.core.domain.model.MoonPhase

object IconManager {

    private const val MAIN_ACTIVITY = "com.example.moon.MainActivity"

    private val phaseToAliasN = mapOf(
        MoonPhase.NEW to "${MAIN_ACTIVITY}NewMoon",
        MoonPhase.WAXING_CRESCENT to "${MAIN_ACTIVITY}WaxingCrescent",
        MoonPhase.FIRST_QUARTER to "${MAIN_ACTIVITY}FirstQuarter",
        MoonPhase.WAXING_GIBBOUS to "${MAIN_ACTIVITY}WaxingGibbous",
        MoonPhase.FULL to MAIN_ACTIVITY,
        MoonPhase.WANING_GIBBOUS to "${MAIN_ACTIVITY}WaningGibbous",
        MoonPhase.LAST_QUARTER to "${MAIN_ACTIVITY}LastQuarter",
        MoonPhase.WANING_CRESCENT to "${MAIN_ACTIVITY}WaningCrescent"
    )

    private val phaseToAliasS = mapOf(
        MoonPhase.NEW to "${MAIN_ACTIVITY}NewMoonS",
        MoonPhase.WAXING_CRESCENT to "${MAIN_ACTIVITY}WaxingCrescentS",
        MoonPhase.FIRST_QUARTER to "${MAIN_ACTIVITY}FirstQuarterS",
        MoonPhase.WAXING_GIBBOUS to "${MAIN_ACTIVITY}WaxingGibbousS",
        MoonPhase.FULL to "${MAIN_ACTIVITY}FullMoonS",
        MoonPhase.WANING_GIBBOUS to "${MAIN_ACTIVITY}WaningGibbousS",
        MoonPhase.LAST_QUARTER to "${MAIN_ACTIVITY}LastQuarterS",
        MoonPhase.WANING_CRESCENT to "${MAIN_ACTIVITY}WaningCrescentS"
    )

    fun updateIconForPhase(context: Context, currentPhase: MoonPhase, latitude: Double) {
        val isSouthern = latitude < 0
        val targetAlias = if (isSouthern) phaseToAliasS[currentPhase] else phaseToAliasN[currentPhase]
        if (targetAlias == null) return

        try {
            val packageManager = context.packageManager
            val targetComponentName = ComponentName(context.packageName, targetAlias)

            val targetState = packageManager.getComponentEnabledSetting(targetComponentName)
            val isTargetEnabled = if (targetAlias == MAIN_ACTIVITY) {
                (targetState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) || (targetState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
            } else {
                targetState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }

            val allAliases = (phaseToAliasN.values + phaseToAliasS.values).filter { it != MAIN_ACTIVITY }
            val aliasesToDisable = allAliases.filter { alias ->
                alias != targetAlias && packageManager.getComponentEnabledSetting(ComponentName(context.packageName, alias)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }

            if (isTargetEnabled && aliasesToDisable.isEmpty()) {
                return
            }

            val mainComponentName = ComponentName(context.packageName, MAIN_ACTIVITY)
            if (packageManager.getComponentEnabledSetting(mainComponentName) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                packageManager.setComponentEnabledSetting(
                    mainComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            }

            if (targetAlias != MAIN_ACTIVITY && !isTargetEnabled) {
                packageManager.setComponentEnabledSetting(
                    targetComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            }

            aliasesToDisable.forEach { alias ->
                packageManager.setComponentEnabledSetting(
                    ComponentName(context.packageName, alias),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        } catch (_: Exception) {
            // Ignore PackageManager errors to prevent startup crashes
        }
    }
}
