package com.notes.notesbackend.controller

import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.notes.notesbackend.database.model.Note
import com.notes.notesbackend.database.repository.NoteRepository
import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant


// POST http://localhost:8085/notes
// GET http://localhost:8085/notes?ownerId=123
// DELETE http://localhost:8085/notes/123


@RestController
@RequestMapping("/notes")
class NoteController(private val noteRepository: NoteRepository) {


    data class NoteRequest(
        val id: String?,
        @NotBlank(message = "Title cant't be blank.")
        val title: String,
        val content: String,
        val color: Long
    )

    data class NoteResponse(
        val id: String,
        val title: String,
        val content: String,
        val color: Long,
        val createdAt: Instant
    )

    @PostMapping
    fun save(@RequestBody body: NoteRequest): NoteResponse {

        val ownerId = SecurityContextHolder.getContext().authentication.principal as String


        val note = noteRepository.save<Note>(
            Note(
                id = body.id?.let { ObjectId(it) } ?: ObjectId.get(),
                title = body.title,
                content = body.title,
                color = body.color,
                createdAt = Instant.now(),
                ownerId = ObjectId(ownerId)

            )

        )
        return note.toResponse()

    }


    @GetMapping
    fun findByOwnerId(): List<NoteResponse> {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        return noteRepository.findByOwnerId(ObjectId(ownerId)).map { note ->
            note.toResponse()


        }
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteById(@PathVariable id: String) {
        val note = noteRepository.findById((ObjectId(id))).orElseThrow() {
            IllegalArgumentException("Note not found")
        }
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        if (note.ownerId.toHexString() == ownerId) {
            noteRepository.deleteById(ObjectId(id))

        }

        noteRepository.deleteById(ObjectId(id))
    }


    private fun Note.toResponse(): NoteController.NoteResponse {
        return NoteResponse(
            id = id.toHexString(),
            title = title,
            content = content,
            color = color,
            createdAt = createdAt

        )
    }


}