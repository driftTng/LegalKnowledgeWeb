package com.legalknowledge.controller;

import com.legalknowledge.dto.response.LegalDocumentDTO;
import com.legalknowledge.service.LegalDocumentService;
import com.legalknowledge.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台管理控制器，所有接口需要 ADMIN 角色
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final LegalDocumentService documentService;
    private final UserService userService;

    public AdminController(LegalDocumentService documentService, UserService userService) {
        this.documentService = documentService;
        this.userService = userService;
    }

    // region 法规管理

    @PostMapping("/documents")
    public ResponseEntity<?> create(@Valid @RequestBody LegalDocumentDTO dto) {
        try {
            return ResponseEntity.ok(documentService.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/documents/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @Valid @RequestBody LegalDocumentDTO dto) {
        try {
            return ResponseEntity.ok(documentService.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            documentService.delete(id);
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // endregion

    // region 用户权限管理

    /** 用户列表 */
    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        return ResponseEntity.ok(userService.listAll());
    }

    /** 修改用户角色 */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String role = body.get("role");
            return ResponseEntity.ok(userService.updateRole(id, role));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // endregion
}
