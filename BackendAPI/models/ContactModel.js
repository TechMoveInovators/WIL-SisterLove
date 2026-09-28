let contactEnquiries = [];

const ContactModel = {

    // Get all contact enquiries
    getAll: () => {
        return contactEnquiries;
    },

    // Get contact enquiry by ID
    getById: (id) => {
        return contactEnquiries.find(
            contact => contact.id === Number(id)
        );
    },

    // Create contact enquiry
    create: (contactData) => {

        const newContact = {
            id: contactEnquiries.length + 1,
            name: contactData.name,
            email: contactData.email,
            phone: contactData.phone || "",
            subject: contactData.subject,
            message: contactData.message,
            status: "New",
            createdAt: new Date()
        };

        contactEnquiries.push(newContact);

        return newContact;
    }
};

module.exports = ContactModel;