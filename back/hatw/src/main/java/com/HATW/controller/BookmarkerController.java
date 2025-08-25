package com.HATW.controller;

import com.HATW.dto.BookmarkerDTO;
import com.HATW.dto.UserDTO;
import com.HATW.service.BookmarkerService;
import com.HATW.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/bookmarker")
@CrossOrigin(origins = "http://localhost:5173")
public class BookmarkerController {

    private final BookmarkerService bookmarkService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<BookmarkerDTO>> list(
            @RequestHeader("Authorization") String token) {
        // UserService에서 토큰으로 UserDTO 조회
        UserDTO user = userService.getUserInfoFromToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<BookmarkerDTO> bookmarker = bookmarkService.findByUserId(user.getUserId());
        if (bookmarker.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(bookmarker);
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader("Authorization") String token,
            @RequestBody BookmarkerDTO bookmarker) {
        UserDTO user = userService.getUserInfoFromToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        bookmarker.setUserId(user.getUserId());
        bookmarkService.insertBookmarker(bookmarker);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{idx}")
    public ResponseEntity<?> delete(
            @RequestHeader("Authorization") String token,
            @PathVariable int idx) {

        UserDTO user = userService.getUserInfoFromToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        bookmarkService.deleteBookmarker(idx, user.getUserId());
        return ResponseEntity.noContent().build();
    }
}