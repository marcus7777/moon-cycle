package com.example.moon.core.domain.model

data class IfsPrompt(
    val title: String,
    val prompt: String
) {
    val fullText: String get() = "$title\n\"$prompt\""
}

object IfsPromptProvider {

    val prompts: List<IfsPrompt> = listOf(
        IfsPrompt(
            "New Moon (Invitation)",
            "The moon rests in darkness again. I wonder—what Part within me is already awake, quietly waiting to be noticed? Can I, from Self, simply say, 'I see you. There’s no rush, no demand—just space. Would you like to walk this cycle with me?'"
        ),
        IfsPrompt(
            "Day 1 (Tending the Spark)",
            "The faintest light returns. Is there a Part that feels hesitant to step forward, unsure if it’s safe to be seen? Can I, from Self, offer a quiet invitation: 'You don’t have to be ready. Just being here is enough. I’m listening'?"
        ),
        IfsPrompt(
            "Day 2 (Gentle Momentum)",
            "The light barely visible. Which Part is eager to push forward, and which one hesitates? Can you write from the one that needs to talk most?"
        ),
        IfsPrompt(
            "Day 3 (Holding Space)",
            "The light is still so soft. Is there a Part that’s been overlooked, not because it’s loud, but because it’s been quiet for so long? Gently say, 'I don’t need you to prove your worth. You belong here, just as you are'?"
        ),
        IfsPrompt(
            "Day 4 (Soft Holding)",
            "The moon’s glow is still tender. Is there a Part that wants to withdraw, to wait until things feel safer? Can we simply keep the space warm—no pressure, no push—just a quiet 'I’m here' to every part within?"
        ),
        IfsPrompt(
            "Day 5 (Whispers of Alignment)",
            "The moon carries a little more light tonight. Is there a Part softly tugging my attention—maybe one I’ve overlooked? Can we lean in with kindness and ask, 'What do you need for me to hear you?'"
        ),
        IfsPrompt(
            "Day 6 (Bridging Inner Worlds)",
            "The light is growing, and so is the pull toward action. Are any Parts starting to align around the intention, while others still linger in doubt? Can I, from Self, welcome both—inviting movement without leaving anyone behind?"
        ),
        IfsPrompt(
            "Day 7 (Tending the Spark)",
            "The moon is finding its shape, and so am I. Is there a Part that’s starting to believe in this path? Can I, from Self, gently celebrate that flicker of faith—without rushing ahead—just tending the spark with care?"
        ),
        IfsPrompt(
            "Waxing Moon (Growth)",
            "Which 'Manager' Parts are working hard to achieve my goals right now? Can I acknowledge their effort with Self-compassion?"
        ),
        IfsPrompt(
            "Day After Waxing (Balancing Effort)",
            "As action builds, am I noticing any Parts taking on too much? Can I, from Self, pause and ask, 'Who needs relief so we can move forward together—not just pushed by the busiest Parts?'"
        ),
        IfsPrompt(
            "Mid-Waxing (Inviting Collaboration)",
            "The moon grows fuller, and so does the call to act. Are there Parts that have been quiet—maybe creative or playful ones—waiting to contribute? Can I, from Self, gently invite them in, so growth doesn’t just come from effort, but from aliveness?"
        ),
        IfsPrompt(
            "Approaching Fullness (Holding Tension)",
            "The light is nearly full, and so is my inner world. Are there Parts in conflict—one pushing forward, another pulling back? Can I, from Self, hold the tension without needing to fix it, letting wholeness include both?"
        ),
        IfsPrompt(
            "Deepening Light (Welcoming the Edges)",
            "The fullness is near, and so is the intensity. Are there Parts I’ve been avoiding—maybe too loud, too raw, or too tender? Can I, from Self, turn toward them not to change them, but to say, 'You belong here too'?"
        ),
        IfsPrompt(
            "Near the Peak (Softening the Edges)",
            "The light is almost full, and so is the pressure. Is there a Part trying to 'get it right' for everyone? Can I, from Self, gently remind them that wholeness isn’t about perfection—but about presence, even in the wobble?"
        ),
        IfsPrompt(
            "Threshold of Fullness (Listening to the Hush)",
            "The moon holds its breath before the peak. In the stillness, is there a Part speaking in whispers—maybe one I’ve silenced to stay strong? Can I, from Self, lean in close and let that quiet voice be the one that guides me now?"
        ),
        IfsPrompt(
            "The Brightening (Honouring the Build)",
            "The moon is almost full, and so is my inner landscape. Are there Parts that have carried quiet burdens to get me here? Can I, from Self, pause and say, 'I see you, and I thank you—for your strength, your silence, your steady hold'?"
        ),
        IfsPrompt(
            "The Final Approach (Surrender Before the Peak)",
            "One breath from fullness. Is there a Part still trying to control how this unfolds? Can I, from Self, gently invite it to rest—not by force, but by offering, 'I’ve got us. You don’t have to hold on so tightly any more'?"
        ),
        IfsPrompt(
            "Full Moon (Release)",
            "In this peak energy, am I noticing any 'Firefighter' Parts reacting to intensity? How can I bring Self-presence to soothe the flames?"
        ),
        IfsPrompt(
            "The Turn (Softening the Glow)",
            "The moon begins its gentle release. Is there a Part that wants to hold on tight to this peak—afraid of what fades with the light? Can I, from Self, offer a steady hand, reminding them that letting go isn’t loss, but trust in the cycle?"
        ),
        IfsPrompt(
            "Releasing the Charge (Updating)",
            "The moon is turning, and so is the energy. Are there 'Firefighter' Parts still acting from an old threat—holding a picture of danger that’s no longer true? Can I, from Self, gently offer a new image: 'Look around. See the light, feel the breath. We’re not there any more. We’re here'?"
        ),
        IfsPrompt(
            "Updating the Inner Map",
            "The light continues to soften. Is there a Part still braced for a storm that has already passed—holding onto an old picture of danger? Can I, from Self, gently offer a new view: 'Look around. Feel the stillness. The threat is gone. We’re here, in this quiet, and we’re safe'?"
        ),
        IfsPrompt(
            "Curious Inquiry",
            "The light is still fading. I wonder—what’s it like for the Parts who’ve been on watch? What are they noticing now, as the intensity softens? And if they’re still holding tension, what world are they seeing—one that’s still stormy, or one that’s already calm?"
        ),
        IfsPrompt(
            "Tending the Quiet",
            "The moon is less full now, and the energy is turning inward. I wonder—what’s it like for the Parts who’ve been loud or active? Are they winding down on their own, or is there a part of me that’s unsure how to let go of the charge? Can I, from Self, simply ask: 'What do you need to feel safe in this stillness?'"
        ),
        IfsPrompt(
            "Listening Beneath the Surface",
            "The moon continues to wane, and the inner world grows quieter. I wonder—what’s it like for the Parts who rarely speak up? Are they resting, waiting, or simply feeling unseen? Can I, from Self, gently ask: 'What have you been holding? And what would it feel like to let it be known, just a little?'"
        ),
        IfsPrompt(
            "Waning Moon (Reflection)",
            "As the light fades, are there any 'Exile' Parts carrying old burdens that are ready to be seen? What does my Self-leadership look like for them tonight?"
        ),
        IfsPrompt(
            "Honouring the Hidden",
            "The dark is growing, and so is the invitation to listen. Is there a Part that’s been exiled long ago, still holding a story that’s never been told? Can I, from Self, gently ask: 'What do you need for me to finally hear you—not to fix, but to witness?'"
        ),
        IfsPrompt(
            "Approaching the Dark (Tender Witnessing)",
            "The moon is nearly gone, and the inner world feels hushed. Is there a Part carrying an old wound that’s been hidden, not because it wants to stay buried, but because it’s waited so long to be met with kindness? Can I, from Self, offer not solutions, but soft presence—just saying, 'I’m here. You don’t have to carry this alone any more'?"
        ),
        IfsPrompt(
            "Deepening Stillness (Compassionate Holding)",
            "The light is almost gone, and the silence grows. Is there a Part that’s been afraid to speak, not because it’s angry, but because it’s tender—afraid of being too much, or not enough? Can I, from Self, simply say: 'You are safe here. Your softness is not weakness. I’ve got you'?"
        ),
        IfsPrompt(
            "Threshold of the Dark (Sacred Waiting)",
            "We’re nearing the moon’s return to dark. Is there a Part that feels empty, as if something’s missing? Can I, from Self, gently remind it: 'This isn’t loss—it’s preparation. The void isn’t empty; it’s full of what’s waiting to be born'?"
        ),
        IfsPrompt(
            "On the Edge of Return (Whispering Gratitude)",
            "In this deepest quiet, I wonder—what would it feel like to thank the Parts who’ve carried the weight, even when I didn’t know their names? Can I, from Self, offer a quiet gratitude: 'Thank you for holding on. I see you now. And I’m here to hold you'?"
        ),
        IfsPrompt(
            "In the Quiet (Soft Reassurance)",
            "The moon is dark now, and the world feels still. Is there a Part that fears this emptiness, as if stillness means absence? Can I, from Self, gently whisper: 'This is not abandonment. This is belonging. You are not alone in the dark—I’m right here with you'?"
        ),
        IfsPrompt(
            "Just Before the New (Tending the Embers)",
            "The cycle is about to turn. Beneath the silence, is there a Part that’s been waiting—not demanding, just hoping to be seen? Can I, from Self, lean in close and say: 'I know you’ve been here all along. Thank you for your patience. Let’s begin again, together'?"
        )
    )

    fun getPromptForAge(moonAgeDays: Double): IfsPrompt {
        val synodicMonthDays = 29.53059
        val normalized = ((moonAgeDays % synodicMonthDays) + synodicMonthDays) % synodicMonthDays / synodicMonthDays
        val index = (normalized * prompts.size).toInt().coerceIn(0, prompts.size - 1)
        return prompts[index]
    }

    fun getPromptForDayOfMonth(dayOfMonth: Int): IfsPrompt {
        val index = ((dayOfMonth - 1) % prompts.size + prompts.size) % prompts.size
        return prompts[index]
    }
}
