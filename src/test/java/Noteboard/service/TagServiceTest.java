package Noteboard.service;

import Noteboard._helpers.ResourceNotFoundException;
import Noteboard._helpers.ValidationException;
import Noteboard.model.Note;
import Noteboard.model.Tag;
import Noteboard.repository.NoteRepository;
import Noteboard.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TagServiceTest {

    private TagService tagService;
    private TagRepository tagRepository;
    private NoteRepository noteRepository;

    @BeforeEach
    void setUp() {
        tagService = new TagService();
        tagRepository = mock(TagRepository.class);
        noteRepository = mock(NoteRepository.class);

        tagService.tagRepository = tagRepository;
        tagService.noteRepository = noteRepository;
    }

    @Test
    void findAll_returnsAllTags() {
        Tag tag1 = new Tag("Tag1");
        Tag tag2 = new Tag("Tag2");
        when(tagRepository.listAll()).thenReturn(List.of(tag1, tag2));

        List<Tag> result = tagService.findAll();

        assertEquals(2, result.size());
        verify(tagRepository).listAll();
    }

    @Test
    void findById_returnsTagWhenExists() {
        Tag tag = new Tag("Test Tag");
        tag.setId(1L);
        when(tagRepository.findById(1L)).thenReturn(tag);

        Tag result = tagService.findById(1L);

        assertNotNull(result);
        assertEquals("Test Tag", result.name);
        verify(tagRepository).findById(1L);
    }

    @Test
    void findById_returnsNullWhenNotExists() {
        when(tagRepository.findById(999L)).thenReturn(null);

        Tag result = tagService.findById(999L);

        assertNull(result);
        verify(tagRepository).findById(999L);
    }

    @Test
    void findByName_returnsTagWhenExists() {
        Tag tag = new Tag("Test Tag");
        when(tagRepository.findByName("Test Tag")).thenReturn(tag);

        Tag result = tagService.findByName("Test Tag");

        assertNotNull(result);
        assertEquals("Test Tag", result.name);
        verify(tagRepository).findByName("Test Tag");
    }

    @Test
    void findByName_returnsNullWhenNotExists() {
        when(tagRepository.findByName("NonExistent")).thenReturn(null);

        Tag result = tagService.findByName("NonExistent");

        assertNull(result);
        verify(tagRepository).findByName("NonExistent");
    }

    @Test
    void createTag_throwsWhenTagIsNull() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> tagService.createTag(null));
        assertEquals("Tag cannot be null", ex.getMessage());
    }

    @Test
    void createTag_throwsWhenNameIsNull() {
        Tag tag = new Tag();
        tag.name = null;

        ValidationException ex = assertThrows(ValidationException.class,
                () -> tagService.createTag(tag));
        assertEquals("Tag name is required", ex.getMessage());
    }

    @Test
    void createTag_throwsWhenNameIsEmpty() {
        Tag tag = new Tag();
        tag.name = "   ";

        ValidationException ex = assertThrows(ValidationException.class,
                () -> tagService.createTag(tag));
        assertEquals("Tag name is required", ex.getMessage());
    }

    @Test
    void createTag_throwsWhenTagNameAlreadyExists() {
        Tag tag = new Tag();
        tag.name = "Existing Tag";
        Tag existingTag = new Tag("Existing Tag");

        when(tagRepository.findByName("Existing Tag")).thenReturn(existingTag);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> tagService.createTag(tag));
        assertTrue(ex.getMessage().contains("Tag with name 'Existing Tag' already exists"));
    }

    @Test
    void createTag_persistsTagWithTrimmedName() {
        Tag tag = new Tag();
        tag.name = "  Test Tag  ";

        when(tagRepository.findByName("Test Tag")).thenReturn(null);

        Tag result = tagService.createTag(tag);

        assertEquals("Test Tag", result.name);
        verify(tagRepository).findByName("Test Tag");
        verify(tagRepository).persist(result);
    }

    @Test
    void updateTag_throwsWhenTagNotFound() {
        Tag tag = new Tag();
        tag.name = "New Name";

        when(tagRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> tagService.updateTag(999L, tag));
        assertTrue(ex.getMessage().contains("Tag with ID 999 not found"));
    }

    @Test
    void updateTag_updatesNameWhenProvided() {
        Tag existingTag = new Tag("Old Name");
        existingTag.setId(1L);
        Tag updateTag = new Tag();
        updateTag.name = "New Name";

        when(tagRepository.findById(1L)).thenReturn(existingTag);
        when(tagRepository.findByName("New Name")).thenReturn(null);

        Tag result = tagService.updateTag(1L, updateTag);

        assertEquals("New Name", result.name);
        verify(tagRepository).findByName("New Name");
        verify(tagRepository).persist(existingTag);
    }

    @Test
    void updateTag_throwsWhenNewNameAlreadyExists() {
        Tag existingTag = new Tag("Old Name");
        existingTag.setId(1L);
        Tag otherTag = new Tag("New Name");
        otherTag.setId(2L);
        Tag updateTag = new Tag();
        updateTag.name = "New Name";

        when(tagRepository.findById(1L)).thenReturn(existingTag);
        when(tagRepository.findByName("New Name")).thenReturn(otherTag);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> tagService.updateTag(1L, updateTag));
        assertTrue(ex.getMessage().contains("Tag with name 'New Name' already exists"));
    }

    @Test
    void updateTag_allowsSameNameForSameTag() {
        Tag existingTag = new Tag("Same Name");
        existingTag.setId(1L);
        Tag updateTag = new Tag();
        updateTag.name = "Same Name";

        when(tagRepository.findById(1L)).thenReturn(existingTag);
        when(tagRepository.findByName("Same Name")).thenReturn(existingTag);

        Tag result = tagService.updateTag(1L, updateTag);

        assertEquals("Same Name", result.name);
        verify(tagRepository).persist(existingTag);
    }

    @Test
    void updateTag_ignoresEmptyName() {
        Tag existingTag = new Tag("Old Name");
        existingTag.setId(1L);
        Tag updateTag = new Tag();
        updateTag.name = "   ";

        when(tagRepository.findById(1L)).thenReturn(existingTag);

        Tag result = tagService.updateTag(1L, updateTag);

        assertEquals("Old Name", result.name);
        verify(tagRepository).persist(existingTag);
    }

    @Test
    void deleteTag_throwsWhenTagNotFound() {
        when(tagRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> tagService.deleteTag(999L));
        assertTrue(ex.getMessage().contains("Tag with ID 999 not found"));
    }

    @Test
    void deleteTag_deletesTagWhenExists() {
        Tag tag = new Tag("Test Tag");
        tag.setId(1L);
        when(tagRepository.findById(1L)).thenReturn(tag);

        tagService.deleteTag(1L);

        verify(tagRepository).findById(1L);
        verify(tagRepository).delete(tag);
    }

    @Test
    void addTagToNote_throwsWhenNoteNotFound() {
        when(noteRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> tagService.addTagToNote(999L, 1L));
        assertTrue(ex.getMessage().contains("Note with ID 999 not found"));
    }

    @Test
    void addTagToNote_throwsWhenTagNotFound() {
        Note note = new Note("Title", "Content");
        note.setId(1L);
        when(noteRepository.findById(1L)).thenReturn(note);
        when(tagRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> tagService.addTagToNote(1L, 999L));
        assertTrue(ex.getMessage().contains("Tag with ID 999 not found"));
    }

    @Test
    void addTagToNote_addsTagToNote() {
        Note note = new Note("Title", "Content");
        note.setId(1L);
        note.tags = new ArrayList<>();
        Tag tag = new Tag("Test Tag");
        tag.setId(1L);

        when(noteRepository.findById(1L)).thenReturn(note);
        when(tagRepository.findById(1L)).thenReturn(tag);

        Note result = tagService.addTagToNote(1L, 1L);

        assertTrue(result.tags.contains(tag));
        assertEquals(1, result.tags.size());
        verify(noteRepository).persist(note);
    }

    @Test
    void addTagToNote_initializesTagsListWhenNull() {
        Note note = new Note("Title", "Content");
        note.setId(1L);
        note.tags = null;
        Tag tag = new Tag("Test Tag");
        tag.setId(1L);

        when(noteRepository.findById(1L)).thenReturn(note);
        when(tagRepository.findById(1L)).thenReturn(tag);

        Note result = tagService.addTagToNote(1L, 1L);

        assertNotNull(result.tags);
        assertTrue(result.tags.contains(tag));
        verify(noteRepository).persist(note);
    }

    @Test
    void addTagToNote_doesNotAddDuplicateTag() {
        Note note = new Note("Title", "Content");
        note.setId(1L);
        Tag tag = new Tag("Test Tag");
        tag.setId(1L);
        note.tags = new ArrayList<>();
        note.tags.add(tag);

        when(noteRepository.findById(1L)).thenReturn(note);
        when(tagRepository.findById(1L)).thenReturn(tag);

        Note result = tagService.addTagToNote(1L, 1L);

        assertEquals(1, result.tags.size());
        verify(noteRepository).persist(note);
    }

    @Test
    void removeTagFromNote_throwsWhenNoteNotFound() {
        when(noteRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> tagService.removeTagFromNote(999L, 1L));
        assertTrue(ex.getMessage().contains("Note with ID 999 not found"));
    }

    @Test
    void removeTagFromNote_throwsWhenTagNotFound() {
        Note note = new Note("Title", "Content");
        note.setId(1L);
        when(noteRepository.findById(1L)).thenReturn(note);
        when(tagRepository.findById(999L)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> tagService.removeTagFromNote(1L, 999L));
        assertTrue(ex.getMessage().contains("Tag with ID 999 not found"));
    }

    @Test
    void removeTagFromNote_removesTagFromNote() {
        Note note = new Note("Title", "Content");
        note.setId(1L);
        Tag tag = new Tag("Test Tag");
        tag.setId(1L);
        note.tags = new ArrayList<>();
        note.tags.add(tag);

        when(noteRepository.findById(1L)).thenReturn(note);
        when(tagRepository.findById(1L)).thenReturn(tag);

        Note result = tagService.removeTagFromNote(1L, 1L);

        assertFalse(result.tags.contains(tag));
        assertEquals(0, result.tags.size());
        verify(noteRepository).persist(note);
    }

    @Test
    void removeTagFromNote_handlesNullTagsList() {
        Note note = new Note("Title", "Content");
        note.setId(1L);
        note.tags = null;
        Tag tag = new Tag("Test Tag");
        tag.setId(1L);

        when(noteRepository.findById(1L)).thenReturn(note);
        when(tagRepository.findById(1L)).thenReturn(tag);

        Note result = tagService.removeTagFromNote(1L, 1L);

        assertNull(result.tags);
        verify(noteRepository).persist(note);
    }
}
