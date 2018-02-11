package dev.olog.msc.presentation.thanks.di

import dagger.Subcomponent
import dagger.android.AndroidInjector
import dev.olog.msc.dagger.PerFragment
import dev.olog.msc.presentation.thanks.SpecialThanksFragment

@Subcomponent(modules = arrayOf(
        SpecialThanksFragmentModule::class
))
@PerFragment
interface SpecialThanksFragmentSubComponent : AndroidInjector<SpecialThanksFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<SpecialThanksFragment>() {

        abstract fun module(module: SpecialThanksFragmentModule): Builder

        override fun seedInstance(instance: SpecialThanksFragment) {
            module(SpecialThanksFragmentModule(instance))
        }
    }

}