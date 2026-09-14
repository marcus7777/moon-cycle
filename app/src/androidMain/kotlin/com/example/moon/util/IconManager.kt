package com.example.moon.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.moon.R
import com.example.moon.domain.model.MoonPhase

object IconManager {

    private val phaseToAlias = mapOf(
        MoonPhase.NEW to "com.example.moon.MainActivityNewMoon",
        MoonPhase.WAXING_CRESCENT to "com.example.moon.MainActivityWaxingCrescent",
        MoonPhase.FIRST_QUARTER to "com.example.moon.MainActivityFirstQuarter",
        MoonPhase.WAXING_GIBBOUS to "com.example.moon.MainActivityWaxingGibbous",
        MoonPhase.FULL to "com.example.moon.MainActivity",
        MoonPhase.WANING_GIBBOUS to "com.example.moon.MainActivityWaningGibbous",
        MoonPhase.LAST_QUARTER to "com.example.moon.MainActivityLastQuarter",
        MoonPhase.WANING_CRESCENT to "com.example.moon.MainActivityWaningCrescent"
    )

    fun getDrawableForPhase(phase: MoonPhase): Int {
        return when (phase) {
            MoonPhase.NEW -> R.drawable.moon_new
            MoonPhase.WAXING_CRESCENT -> R.drawable.moon_waxing_crescent
            MoonPhase.FIRST_QUARTER -> R.drawable.moon_first_quarter
            MoonPhase.WAXING_GIBBOUS -> R.drawable.moon_waxing_gibbous
            MoonPhase.FULL -> R.drawable.moon_full
            MoonPhase.WANING_GIBBOUS -> R.drawable.moon_waning_gibbous
            MoonPhase.LAST_QUARTER -> R.drawable.moon_last_quarter
            MoonPhase.WANING_CRESCENT -> R.drawable.moon_waning_crescent
        }
    }

    fun updateIconForPhase(context: Context, currentPhase: MoonPhase) {
        val targetAlias = phaseToAlias[currentPhase] ?: return
        val packageManager = context.packageManager

        val componentName = ComponentName(context.packageName, targetAlias)

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

        // Disable all other aliases
        phaseToAlias.values.filter { it != targetAlias }.forEach { alias ->
            packageManager.setComponentEnabledSetting(
                ComponentName(context.packageName, alias),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
