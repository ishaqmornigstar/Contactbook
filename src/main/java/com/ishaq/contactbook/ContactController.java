package com.ishaq.contactbook;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactRepository repository;

    public ContactController(ContactRepository repository) { this.repository = repository; }

    @GetMapping
    public List<Contact> list(@RequestParam(defaultValue = "") String search) {
        if (search.length() > 300) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search is too long");
        return repository.findAll(search.trim());
    }

    @GetMapping("/{id}")
    public Contact get(@PathVariable long id) {
        return repository.findById(id).orElseThrow(ContactController::notFound);
    }

    @PostMapping
    public ResponseEntity<Contact> create(@Valid @RequestBody ContactRequest request) {
        Contact saved = repository.create(request);
        return ResponseEntity.created(URI.create("/api/contacts/" + saved.id())).body(saved);
    }

    @PutMapping("/{id}")
    public Contact update(@PathVariable long id, @Valid @RequestBody ContactRequest request) {
        if (!repository.update(id, request)) throw notFound();
        return new Contact(id, request.name(), request.phone(), request.email(), request.address());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        if (!repository.delete(id)) throw notFound();
        return ResponseEntity.noContent().build();
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found");
    }
}
