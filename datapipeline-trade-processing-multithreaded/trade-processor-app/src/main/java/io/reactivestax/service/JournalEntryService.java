package io.reactivestax.service;

import io.reactivestax.domain.JournalEntry;

public interface JournalEntryService {
    String saveJournalEntry(JournalEntry journalEntry);
}
