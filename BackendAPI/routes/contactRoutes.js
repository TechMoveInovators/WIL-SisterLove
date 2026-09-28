const express = require("express");

const {
    createContact,
    getContacts,
    getContactById
} = require("../controllers/ContactController");

const router = express.Router();

// POST - Create contact enquiry
router.post("/", createContact);

// GET - Get all contact enquiries
router.get("/", getContacts);

// GET - Get one contact enquiry
router.get("/:id", getContactById);

module.exports = router;