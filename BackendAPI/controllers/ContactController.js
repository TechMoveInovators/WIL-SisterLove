const ContactModel = require("../models/ContactModel");

// Create contact enquiry
const createContact = async (req, res) => {
    try {
        const {
            name,
            email,
            phone,
            subject,
            message
        } = req.body;

        // Validate required fields
        if (
            typeof name !== "string" || !name.trim() ||
            typeof email !== "string" || !email.trim() ||
            typeof subject !== "string" || !subject.trim() ||
            typeof message !== "string" || !message.trim()
        ) {
            return res.status(400).json({
                message: "Name, email, subject and message are required"
            });
        }

        // Validate field lengths against the SQL Server schema
        if (
            name.trim().length > 150 ||
            email.trim().length > 150 ||
            subject.trim().length > 200 ||
            message.trim().length > 3000 ||
            (phone != null && (typeof phone !== "string" || phone.length > 30))
        ) {
            return res.status(400).json({
                message: "One or more fields exceed the allowed length"
            });
        }

        // Basic email format validation
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!emailPattern.test(email.trim())) {
            return res.status(400).json({
                message: "Please provide a valid email address"
            });
        }

        const contact = await ContactModel.create({
            name: name.trim(),
            email: email.trim(),
            phone: phone?.trim() || null,
            subject: subject.trim(),
            message: message.trim()
        });

        return res.status(201).json({
            message: "Contact enquiry submitted successfully",
            contact
        });
    } catch (error) {
        console.error("Error creating contact enquiry:", error);

        return res.status(500).json({
            message: "Unable to submit contact enquiry at this time"
        });
    }
};

// Get all contact enquiries
const getContacts = async (req, res) => {
    try {
        const contacts = await ContactModel.getAll();

        return res.status(200).json({
            message: "Contact enquiries retrieved successfully",
            contacts
        });
    } catch (error) {
        console.error("Error retrieving contact enquiries:", error);

        return res.status(500).json({
            message: "Unable to retrieve contact enquiries at this time"
        });
    }
};

// Get contact enquiry by ID
const getContactById = async (req, res) => {
    try {
        const id = Number(req.params.id);

        if (!Number.isInteger(id) || id <= 0) {
            return res.status(400).json({
                message: "Invalid contact enquiry ID"
            });
        }

        const contact = await ContactModel.getById(id);

        if (!contact) {
            return res.status(404).json({
                message: "Contact enquiry not found"
            });
        }

        return res.status(200).json({
            contact
        });
    } catch (error) {
        console.error("Error retrieving contact enquiry:", error);

        return res.status(500).json({
            message: "Unable to retrieve contact enquiry at this time"
        });
    }
};

module.exports = {
    createContact,
    getContacts,
    getContactById
};