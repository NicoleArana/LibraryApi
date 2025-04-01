package com.example.library.repository;

import com.example.library.model.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByPatronIdAndReturnDateIsNull(Long patronId);
    List<BorrowRecord> findByBookIdAndReturnDateIsNull(Long bookId);
}
