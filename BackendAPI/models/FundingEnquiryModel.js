let fundingEnquiries = [];

const FundingEnquiryModel = {

    // Get all funding enquiries
    getAll: () => {
        return fundingEnquiries;
    },

    // Get one funding enquiry by ID
    getById: (id) => {
        return fundingEnquiries.find(
            enquiry => enquiry.id === Number(id)
        );
    },

    // Create a funding enquiry
    create: (enquiryData) => {

        const newEnquiry = {
            id: fundingEnquiries.length + 1,
            name: enquiryData.name,
            email: enquiryData.email,
            organisation: enquiryData.organisation,
            message: enquiryData.message,
            status: "New",
            submissionDate: new Date()
        };

        fundingEnquiries.push(newEnquiry);

        return newEnquiry;
    }

};

module.exports = FundingEnquiryModel;