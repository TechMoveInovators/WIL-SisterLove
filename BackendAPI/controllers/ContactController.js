const ContactModel = require("../models/ContactModel");

// Create contact enquiry
const createContact = (req, res) => {

    const {
        name,
        email,
        phone,
        subject,
        message
    } = req.body;

    // Validation
    if (!name || !email || !subject || !message) {
        return res.status(400).json({
            message: "Name, email, subject and message are required"
        });
    }

    const contact = ContactModel.create({
        name,
        email,
        phone,
        subject,
        message
    });

    res.status(201).json({
        message: "Contact enquiry submitted successfully",
        contact: contact
    });
};


// Get all contact enquiries
const getContacts = (req, res) => {

    const contacts = ContactModel.getAll();

    res.status(200).json({
        message: "Contact enquiries retrieved successfully",
        contacts: contacts
    });
};


// Get contact enquiry by ID
const getContactById = (req, res) => {

    const contact = ContactModel.getById(req.params.id);

    if (!contact) {
        return res.status(404).json({
            message: "Contact enquiry not found"
        });
    }

    res.status(200).json({
        contact: contact
    });
};


module.exports = {
    createContact,
    getContacts,
    getContactById
};