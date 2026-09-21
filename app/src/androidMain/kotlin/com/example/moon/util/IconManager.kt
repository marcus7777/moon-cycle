package com.example.moon.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.moon.R
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

        val packageManager = context.packageManager
        val componentName = ComponentName(context.packageName, targetAlias)

        // Always ensure the target activity is enabled
        packageManager.setComponentEnabledSetting(
            ComponentName(context.packageName, "com.example.moon.MainActivity"),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        // If already enabled, do nothing
        if (packageManager.getComponentEnabledSetting(componentName) == 
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            return
        }

        // Enable new alias
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        // Disable all other aliases (both N and S)
        val allAliases = phaseToAliasN.values + phaseToAliasS.values
        allAliases.filter { it != targetAlias }.forEach { alias ->
            packageManager.setComponentEnabledSetting(
                ComponentName(context.packageName, alias),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
