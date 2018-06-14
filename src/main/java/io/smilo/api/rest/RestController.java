package io.smilo.api.rest;

import io.smilo.api.rest.models.Status;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.atomic.AtomicLong;

public class RestController {

    private static final String template = "Server is %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/status")
    @ResponseBody
    public Status respondStatus(@RequestParam(name="status", required=false, defaultValue="up") String status) {
        return new Status(counter.incrementAndGet(), String.format(template, status));
    }
}
