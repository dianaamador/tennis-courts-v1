package com.tenniscourts.guests;

import com.tenniscourts.config.BaseRestController;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/guest")
public class GuestController extends BaseRestController {

    private final GuestService guestService;

    @PostMapping("/")
    public ResponseEntity<Void> create(@RequestBody GuestDTO guestDTO) {
        return ResponseEntity.created(locationByEntity(guestService.createOrUpdate(guestDTO).getId())).build();
    }

    @PutMapping("/update")
    public ResponseEntity<GuestDTO> update(@RequestBody GuestDTO guestDTO) {
        return ResponseEntity.ok(guestService.createOrUpdate(guestDTO));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody GuestDTO guestDTO) {
        if (guestService.findById(guestDTO.getId()) == null) {
            return ResponseEntity.noContent().build();
        }
        guestService.delete(guestDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/findById/{id}")
    public ResponseEntity<GuestDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(guestService.findById(id));
    }

    @GetMapping("/findByName/{name}")
    public ResponseEntity<List<GuestDTO>> findByName(@PathVariable String name) {
        return ResponseEntity.ok(guestService.findByName(name));
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<GuestDTO>> findAll() {
        return ResponseEntity.ok(guestService.findAll());
    }
}
