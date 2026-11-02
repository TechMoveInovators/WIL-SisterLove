const DonationModel = require("../models/DonationModel");

const createDonation = async (req, res) => {
    try {
        const {
            name,
            email,
            amount,
            message,
            donationType,
            paymentMethod,
            transactionId
        } = req.body;

        // Validate required fields
        if (
            typeof name !== "string" ||
            !name.trim() ||
            typeof email !== "string" ||
            !email.trim() ||
            amount === undefined ||
            amount === null ||
            amount === ""
        ) {
            return res.status(400).json({
                message: "Name, email and amount are required"
            });
        }

        // Validate email format
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!emailPattern.test(email.trim())) {
            return res.status(400).json({
                message: "Please provide a valid email address"
            });
        }

        // Validate amount and decimal precision
        const numericAmount = Number(amount);

        if (
            !Number.isFinite(numericAmount) ||
            numericAmount <= 0 ||
            !/^\d+(\.\d{1,2})?$/.test(String(amount).trim()) ||
            numericAmount >= 1e16
        ) {
            return res.status(400).json({
                message: "Amount must be positive and have no more than two decimal places"
            });
        }

        // Validate SQL column lengths
        if (name.trim().length > 100 || email.trim().length > 150) {
            return res.status(400).json({
                message: "Name or email exceeds the allowed length"
            });
        }

        if (
            message != null &&
            (typeof message !== "string" || message.length > 500)
        ) {
            return res.status(400).json({
                message: "Message must not exceed 500 characters"
            });
        }

        if (
            donationType != null &&
            (typeof donationType !== "string" ||
             donationType.length > 20)
        ) {
            return res.status(400).json({
                message: "Donation type must not exceed 20 characters"
            });
        }

        if (
            paymentMethod != null &&
            (typeof paymentMethod !== "string" ||
             paymentMethod.length > 50)
        ) {
            return res.status(400).json({
                message: "Payment method must not exceed 50 characters"
            });
        }

        if (
            transactionId != null &&
            (typeof transactionId !== "string" ||
             transactionId.length > 100 ||
             !transactionId.trim())
        ) {
            return res.status(400).json({
                message: "Transaction ID must be a non-empty string of at most 100 characters"
            });
        }

        // Save to Azure SQL.
        // This endpoint records a donation entry; it does not process payment.
        const donation = await DonationModel.create({
            name: name.trim(),
            email: email.trim(),
            amount: numericAmount,
            message: message?.trim() || null,
            donationType: donationType?.trim() || "OneTime",
            paymentMethod: paymentMethod?.trim() || null,
            transactionId: transactionId?.trim() || null,
            userId: null
        });

        return res.status(201).json({
            message: "Donation submitted successfully",
            donation
        });
    } catch (error) {
        console.error("Error creating donation:", error);

        return res.status(500).json({
            message: "Unable to submit donation at this time"
        });
    }
};

const getDonations = async (req, res) => {
    try {
        const donations = await DonationModel.getAll();

        return res.status(200).json({
            message: "Donations retrieved successfully",
            donations
        });
    } catch (error) {
        console.error("Error retrieving donations:", error);

        return res.status(500).json({
            message: "Unable to retrieve donations at this time"
        });
    }
};

const getDonationById = async (req, res) => {
    try {
        const id = Number(req.params.id);

        if (!Number.isInteger(id) || id <= 0) {
            return res.status(400).json({
                message: "Invalid donation ID"
            });
        }

        const donation = await DonationModel.getById(id);

        if (!donation) {
            return res.status(404).json({
                message: "Donation not found"
            });
        }

        return res.status(200).json({ donation });
    } catch (error) {
        console.error("Error retrieving donation:", error);

        return res.status(500).json({
            message: "Unable to retrieve donation at this time"
        });
    }
};

module.exports = {
    createDonation,
    getDonations,
    getDonationById
};