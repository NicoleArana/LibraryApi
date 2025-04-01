package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.model.BorrowRecord;
import com.example.library.model.Patron;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.PatronRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/patrons")
public class PatronController {

    @Autowired
    private PatronRepository patronRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @GetMapping
    public List<Patron> getAllPatrons() {
        return patronRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patron> getPatronById(@PathVariable Long id) {
        return patronRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createPatron(@RequestBody Patron patron) {
        if (patronRepository.existsByEmail(patron.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        return ResponseEntity.ok(patronRepository.save(patron));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patron> updatePatron(@PathVariable Long id, @RequestBody Patron patronDetails) {
        return patronRepository.findById(id)
                .map(patron -> {
                    patron.setFirstName(patronDetails.getFirstName());
                    patron.setLastName(patronDetails.getLastName());
                    patron.setEmail(patronDetails.getEmail());
                    patron.setPhoneNumber(patronDetails.getPhoneNumber());
                    return ResponseEntity.ok(patronRepository.save(patron));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatron(@PathVariable Long id) {
        return patronRepository.findById(id)
                .map(patron -> {
                    patronRepository.delete(patron);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{patronId}/borrow/{bookId}")
    public ResponseEntity<?> borrowBook(@PathVariable Long patronId, @PathVariable Long bookId) {
        var patron = patronRepository.findById(patronId);
        var book = bookRepository.findById(bookId);
        
        if (patron.isEmpty() || book.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if book is already borrowed
        if (!borrowRecordRepository.findByBookIdAndReturnDateIsNull(bookId).isEmpty()) {
            return ResponseEntity.badRequest().body("Book is already borrowed");
        }

        var borrowRecord = new BorrowRecord();
        borrowRecord.setPatron(patron.get());
        borrowRecord.setBook(book.get());
        borrowRecord.setBorrowDate(Timestamp.from(Instant.now()));
        borrowRecord.setDueDate(Timestamp.from(Instant.now().plus(14, ChronoUnit.DAYS)));

        return ResponseEntity.ok(borrowRecordRepository.save(borrowRecord));
    }

    @PostMapping("/borrow/{borrowId}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long borrowId) {
        return borrowRecordRepository.findById(borrowId)
                .map(borrowRecord -> {
                    if (borrowRecord.getReturnDate() != null) {
                        return ResponseEntity.badRequest().body("Book already returned");
                    }
                    borrowRecord.setReturnDate(Timestamp.from(Instant.now()));
                    return ResponseEntity.ok(borrowRecordRepository.save(borrowRecord));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
