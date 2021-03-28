package com.tenniscourts.guests;

import com.tenniscourts.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    private final GuestMapper guestMapper;

    public GuestDTO createOrUpdate(GuestDTO guestDTO) {
        return guestMapper.map(guestRepository.save(guestMapper.map(guestDTO)));
    }

    public void deleteById(Long id) {
        guestRepository.deleteById(id);
    }

    public void delete(GuestDTO guestDTO) {
        guestRepository.delete(guestMapper.map(guestDTO));
    }

    public GuestDTO findById(Long id) {
        return guestRepository.findById(id).map(guestMapper::map).<EntityNotFoundException>orElseThrow(() -> {
            throw new EntityNotFoundException("Guest not found.");
        });
    }

    public List<GuestDTO> findByName(String name) {
        return guestMapper.map(guestRepository.findByName(name));
    }

    public List<GuestDTO> findAll() {
        return guestMapper.map(guestRepository.findAll());
    }
}
