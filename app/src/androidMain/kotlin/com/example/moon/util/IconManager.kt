package com.example.moon.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.moon.core.domain.model.MoonPhase

object IconManager {

    private val phaseToAliasN = mapOf(
        MoonPhase.NEW to "com.example.moon.MainActivityNewMoon",
        MoonPhase.WAXING_CRESCENT to "com.example.moon.MainActivityWaxingCrescent",
        MoonPhase.FIRST_QUARTER to "com.example.moon.MainActivityFirstQuarter",
        MoonPhase.WAXING_GIBBOUS to "com.example.moon.MainActivityWaxingGibbous",
        MoonPhase.FULL to "com.example.moon.MainActivity",
        MoonPhase.WANING_GIBBOUS to "com.example.moon.MainActivityWaningGibbous",
        MoonPhase.LAST_QUARTER to "com.example.moon.MainActivityLastQuarter",
        MoonPhase.WANING_CRESCENT to "com.example.moon.MainActivityWaningCrescent"
    )

    private val phaseToAliasS = mapOf(
        MoonPhase.NEW to "com.example.moon.MainActivityNewMoonS",
        MoonPhase.WAXING_CRESCENT to "com.example.moon.MainActivityWaxingCrescentS",
        MoonPhase.FIRST_QUARTER to "com.example.moon.MainActivityFirstQuarterS",
        MoonPhase.WAXING_GIBBOUS to "com.example.moon.MainActivityWaxingGibbousS",
        MoonPhase.FULL to "com.example.moon.MainActivityFullMoonS",
        MoonPhase.WANING_GIBBOUS to "com.example.moon.MainActivityWaningGibbousS",
        MoonPhase.LAST_QUARTER to "com.example.moon.MainActivityLastQuarterS",
        MoonPhase.WANING_CRESCENT to "com.example.moon.MainActivityWaningCrescentS"
    )

    fun updateIconForPhase(context: Context, currentPhase: MoonPhase, latitude: Double) {
        val isSouthern = latitude < 0
        val targetAlias = if (isSouthern) phaseToAliasS[currentPhase] else phaseToAliasN[currentPhase]
        if (targetAlias == null) return

        try {
            val packageManager = context.packageManager
            val mainActivityName = "com.example.moon.MainActivity"
            val targetComponentName = ComponentName(context.packageName, targetAlias)

            val targetState = packageManager.getComponentEnabledSetting(targetComponentName)
            val isTargetEnabled = if (targetAlias == mainActivityName) {
                (targetState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) || (targetState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
            } else {
                targetState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }

            val allAliases = (phaseToAliasN.values + phaseToAliasS.values).filter { it != mainActivityName }
            val aliasesToDisable = allAliases.filter { alias ->
                alias != targetAlias && packageManager.getComponentEnabledSetting(ComponentName(context.packageName, alias)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }

            if (isTargetEnabled && aliasesToDisable.isEmpty()) {
                return
            }

            val mainComponentName = ComponentName(context.packageName, mainActivityName)
            if (packageManager.getComponentEnabledSetting(mainComponentName) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                packageManager.setComponentEnabledSetting(
                    mainComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            }

            if (targetAlias != mainActivityName && !isTargetEnabled) {
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
