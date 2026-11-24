package com.wortschatz.app.data.repository

import com.wortschatz.app.domain.model.Vocabulary

/** Friendly starter deck so the first round can begin immediately. */
object SeedData {
    private fun word(native: String, foreign: String, language: String, category: String) =
        Vocabulary(
            nativeText = native,
            foreignText = foreign,
            language = language,
            category = category,
        )

    val words: List<Vocabulary> = listOf(
        // French
        word("der Apfel", "la pomme", "Französisch", "Essen"),
        word("das Brot", "le pain", "Französisch", "Essen"),
        word("das Wasser", "l'eau", "Französisch", "Essen"),
        word("das Haus", "la maison", "Französisch", "Alltag"),
        word("der Hund", "le chien", "Französisch", "Tiere"),
        word("die Katze", "le chat", "Französisch", "Tiere"),
        word("der Freund", "l'ami", "Französisch", "Menschen"),
        word("die Schule", "l'école", "Französisch", "Alltag"),
        word("das Buch", "le livre", "Französisch", "Alltag"),
        word("glücklich", "heureux", "Französisch", "Adjektive"),
        word("essen", "manger", "Französisch", "Verben"),
        word("laufen", "courir", "Französisch", "Verben"),

        // Spanish
        word("der Apfel", "la manzana", "Spanisch", "Essen"),
        word("das Brot", "el pan", "Spanisch", "Essen"),
        word("das Wasser", "el agua", "Spanisch", "Essen"),
        word("das Haus", "la casa", "Spanisch", "Alltag"),
        word("der Hund", "el perro", "Spanisch", "Tiere"),
        word("die Katze", "el gato", "Spanisch", "Tiere"),
        word("der Freund", "el amigo", "Spanisch", "Menschen"),
        word("die Schule", "la escuela", "Spanisch", "Alltag"),
        word("das Buch", "el libro", "Spanisch", "Alltag"),
        word("glücklich", "feliz", "Spanisch", "Adjektive"),
        word("essen", "comer", "Spanisch", "Verben"),
        word("laufen", "correr", "Spanisch", "Verben"),

        // English
        word("der Apfel", "the apple", "Englisch", "Essen"),
        word("das Wasser", "the water", "Englisch", "Essen"),
        word("das Haus", "the house", "Englisch", "Alltag"),
        word("der Hund", "the dog", "Englisch", "Tiere"),
        word("das Buch", "the book", "Englisch", "Alltag"),
        word("glücklich", "happy", "Englisch", "Adjektive"),
        word("lernen", "to learn", "Englisch", "Verben"),
        word("der Freund", "the friend", "Englisch", "Menschen"),
    )
}
