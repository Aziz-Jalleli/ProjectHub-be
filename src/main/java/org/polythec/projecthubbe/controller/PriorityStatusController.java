package org.polythec.projecthubbe.controller;

import org.polythec.projecthubbe.entity.Priority;
import org.polythec.projecthubbe.entity.Status;
import org.polythec.projecthubbe.repository.PriorityRepository;
import org.polythec.projecthubbe.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PriorityStatusController {

    @Autowired
    private PriorityRepository priorityRepository;

    @Autowired
    private StatusRepository statusRepository;

    @GetMapping("/priorities")
    public List<Priority> getAllPriorities() {
        return priorityRepository.findAll();
    }

    @GetMapping("/statuses")
    public List<Status> getAllStatuses() {
        return statusRepository.findAll();
    }
}