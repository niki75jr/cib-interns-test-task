package com.n75jr.apitesttask.controller;

import com.n75jr.apitesttask.dao.SockRepository;
import com.n75jr.apitesttask.model.Sock;
import com.n75jr.apitesttask.model.SockID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.function.Predicate;

@RestController
@RequestMapping("/api")
public class SockController {
    @Autowired
    private SockRepository sockRepository;

    // helper methods:
    private boolean isValidSock(Sock sock) {
        boolean result = true;
        if (sock.getColor() == null || sock.getCottonPart() == 0 || sock.getQuantity() == 0) {
            return false;
        }
        if (sock.getCottonPart() <= 0 || sock.getCottonPart() > 100 || sock.getQuantity() <= 0) {
            return false;
        }
        return result;
    }

    private boolean isValidString(String str) {
        boolean result = true;
        if (str == null || str.trim().length() == 0) {
            return false;
        }
        return result;
    }

    // operation-methods:
    private long operationX(Predicate<Sock> predicate) {
        long result = 0;
        Collection<Sock> socks = sockRepository.findAll();
        socks.removeIf(predicate);
        for (Sock sock : socks) {
            result += sock.getQuantity();
        }
        return result;
    }

    private long operationEqual(String color, int cotton) {
        SockID id = new SockID(color, cotton);
        if (!sockRepository.existsById(id)) {
            return 0L;
        }
        return sockRepository.getById(new SockID(color, cotton)).getQuantity();
    }
    //

    @PostMapping("/socks/income")
    public ResponseEntity<Sock> income(@RequestBody Sock sock) {
        if (!isValidSock(sock)) {
            return new ResponseEntity<>(sock, HttpStatus.BAD_REQUEST);
        }
        SockID id = new SockID(sock.getColor(), sock.getCottonPart());
        if (!sockRepository.existsById(id)) {
            sockRepository.save(sock);
            return new ResponseEntity<>(sock, HttpStatus.OK);
        }
        Sock existSock = sockRepository.getById(id);
        sock.setQuantity(existSock.getQuantity() + sock.getQuantity());
        sockRepository.save(sock);
        return new ResponseEntity<>(sock, HttpStatus.OK);
    }

    @PostMapping("/socks/outcome")
    public ResponseEntity<Sock> outcome(@RequestBody Sock sock) {
        if (!isValidSock(sock)) {
            return new ResponseEntity<>(sock, HttpStatus.BAD_REQUEST);
        }
        SockID id = new SockID(sock.getColor(), sock.getCottonPart());
        if (!sockRepository.existsById(id)) {
            return new ResponseEntity<>(sock, HttpStatus.NO_CONTENT);
        }
        Sock existSock = sockRepository.getById(id);
        sock.setQuantity(existSock.getQuantity() - sock.getQuantity());
        if (sock.getQuantity() <= 0) {
            sockRepository.deleteById(id);
        } else
            sockRepository.save(sock);
        return new ResponseEntity<>(sock, HttpStatus.OK);
    }

    // added "required = false" for return of ResponseEntity with "-1" and test
    // added "defaultValue = "-1"" for test
    @GetMapping("/socks")
    public ResponseEntity<Long> socks(@RequestParam(value = "color", required = false) String color,
                                      @RequestParam(value = "cottonPart", required = false, defaultValue = "-1") int cotton,
                                      @RequestParam(value = "operation", required = false) String operation) {
        if (!isValidString(color) || !isValidString(operation) || cotton < 0 || cotton > 100) {
            return new ResponseEntity<>(-1L, HttpStatus.BAD_REQUEST);
        }

        long result = 0;
        Predicate<Sock> predicate = null;
        switch (operation.toLowerCase()) {
            case "morethan":
                predicate = sock -> (!(sock.getColor().equalsIgnoreCase(color) && sock.getCottonPart() > cotton));
                break;
            case "lessthan":
                predicate = sock -> (!(sock.getColor().equalsIgnoreCase(color) && sock.getCottonPart() < cotton));
                break;
            case "equal":
                return new ResponseEntity<>(operationEqual(color, cotton), HttpStatus.OK);
            default:
                return new ResponseEntity<>(-1L, HttpStatus.BAD_REQUEST);
        }
        result = operationX(predicate);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
