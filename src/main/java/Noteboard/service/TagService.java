package Noteboard.service;

import Noteboard.model.Tag;
import Noteboard._helpers.ResourceNotFoundException;
import Noteboard._helpers.ValidationException;
import Noteboard.model.Note;
import Noteboard.repository.TagRepository;
import Noteboard.repository.NoteRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class TagService {

    @Inject
    TagRepository tagRepository;

    @Inject
    NoteRepository noteRepository;

    public List<Tag> findAll() {
        return tagRepository.listAll();
    }

    public Tag findById(Long id) {
        return tagRepository.findById(id);
    }

    public Tag findByName(String name) {
        return tagRepository.findByName(name);
    }

    @Transactional
    public Tag createTag(Tag tag) {
        if (tag == null) {
            throw new ValidationException("Tag cannot be null");
        }
        if (tag.name == null || tag.name.trim().isEmpty()) {
            throw new ValidationException("Tag name is required");
        }
        Tag existingTag = tagRepository.findByName(tag.name.trim());
        if (existingTag != null) {
            throw new ValidationException("Tag with name '" + tag.name + "' already exists");
        }
        tag.setName(tag.name.trim());
        tagRepository.persist(tag);
        return tag;
    }

    @Transactional
    public Tag updateTag(Long id, Tag tag) {
        Tag existingTag = tagRepository.findById(id);
        if (existingTag == null) {
            throw new ResourceNotFoundException("Tag with ID " + id + " not found");
        }
        if (tag.name != null && !tag.name.trim().isEmpty()) {
            Tag tagWithSameName = tagRepository.findByName(tag.name.trim());
            if (tagWithSameName != null && !tagWithSameName.getId().equals(id)) {
                throw new ValidationException("Tag with name '" + tag.name + "' already exists");
            }
            existingTag.setName(tag.name.trim());
        }
        tagRepository.persist(existingTag);
        return existingTag;
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id);
        if (tag == null) {
            throw new ResourceNotFoundException("Tag with ID " + id + " not found");
        }
        tagRepository.delete(tag);
    }

    @Transactional
    public Note addTagToNote(Long noteId, Long tagId) {
        Note note = noteRepository.findById(noteId);
        if (note == null) {
            throw new ResourceNotFoundException("Note with ID " + noteId + " not found");
        }
        Tag tag = tagRepository.findById(tagId);
        if (tag == null) {
            throw new ResourceNotFoundException("Tag with ID " + tagId + " not found");
        }
        if (note.tags == null) {
            note.tags = new java.util.ArrayList<>();
        }
        if (!note.tags.contains(tag)) {
            note.tags.add(tag);
        }
        noteRepository.persist(note);
        return note;
    }

    @Transactional
    public Note removeTagFromNote(Long noteId, Long tagId) {
        Note note = noteRepository.findById(noteId);
        if (note == null) {
            throw new ResourceNotFoundException("Note with ID " + noteId + " not found");
        }
        Tag tag = tagRepository.findById(tagId);
        if (tag == null) {
            throw new ResourceNotFoundException("Tag with ID " + tagId + " not found");
        }
        if (note.tags != null) {
            note.tags.remove(tag);
        }
        noteRepository.persist(note);
        return note;
    }
}

