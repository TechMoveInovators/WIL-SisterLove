const DonationModel = require("../models/DonationModel");

const createDonation = (req, res) => {
    const {
        name,
        email,
        amount,
        message
    } = req.body;

    if (!name || !email || !amount) {
        return res.status(400).json({
            message: "Name, email and amount are required"
        });
    }

    if (Number(amount) <= 0) {
        return res.status(400).json({
            message: "Donation amount must be greater than 0"
        });
    }

    const donation = DonationModel.create({
        name,
        email,
        amount: Number(amount),
        message
    });

    res.status(201).json({
        message: "Donation submitted successfully",
        donation: donation
    });
};

const getDonations = (req, res) => {
    const donations = DonationModel.getAll();

    res.status(200).json({
        message: "Donations retrieved successfully",
        donations: donations
    });
};

const getDonationById = (req, res) => {
    const donation = DonationModel.getById(req.params.id);

    if (!donation) {
        return res.status(404).json({
            message: "Donation not found"
        });
    }

    res.status(200).json({
        donation: donation
    });
};

module.exports = {
    createDonation,
    getDonations,
    getDonationById
};
