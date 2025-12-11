package com.stefanini.api;

import jakarta.validation.constraints.Size;

public class TaskPatchRequest {

    @Size(max = 255)
    private String title;

    @Size(max = 1000)
    private String description;

    private String status;

    public TaskPatchRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
