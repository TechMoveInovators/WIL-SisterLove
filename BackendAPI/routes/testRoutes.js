const express = require("express");

const authenticateToken = require("../middleware/authMiddleware");
const authorizeAdmin = require("../middleware/adminMiddleware");

const router = express.Router();

router.get(
    "/protected",
    authenticateToken,
    (req, res) => {
        res.status(200).json({
            message: "You successfully accessed a protected route.",
            user: req.user
        });
    }
);

router.get(
    "/admin",
    authenticateToken,
    authorizeAdmin,
    (req, res) => {
        res.status(200).json({
            message: "You successfully accessed an administrator-only route.",
            user: req.user
        });
    }
);

module.exports = router;
