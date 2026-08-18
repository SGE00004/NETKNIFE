package com.netknife.tools.phishingsimulator.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Un envio individual de una plantilla a un destinatario, con su seguimiento de clic. */
@Entity
@Table(name = "phishing_simulations")
public class PhishingSimulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", nullable = false)
    private String templateId;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(nullable = false)
    private boolean clicked = false;

    @Column(name = "clicked_at")
    private Instant clickedAt;

    protected PhishingSimulation() {
        // requerido por JPA
    }

    public PhishingSimulation(String templateId, String recipientEmail, Instant sentAt) {
        this.templateId = templateId;
        this.recipientEmail = recipientEmail;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public Instant getClickedAt() {
        return clickedAt;
    }

    public void setClickedAt(Instant clickedAt) {
        this.clickedAt = clickedAt;
    }
}
