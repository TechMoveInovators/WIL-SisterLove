
const jwt = require("jsonwebtoken");
const { getJwtSecret } = require("../controllers/AuthController");

const authenticateToken = (req, res, next) => {
    const authHeader = req.headers.authorization;

    if (!authHeader) {
        return res.status(401).json({
            message: "Access denied. No token provided."
        });
    }

    const [scheme, token] = authHeader.split(" ");

    if (scheme !== "Bearer" || !token) {
        return res.status(401).json({
            message: "Authorization must use Bearer token format."
        });
    }

    try {
        const decoded = jwt.verify(token, getJwtSecret());
        req.user = decoded;
        return next();
    } catch (error) {
        if (error.message === "JWT_SECRET is not configured.") {
            console.error(error.message);

            return res.status(500).json({
                message: "Authentication is not configured."
            });
        }

        return res.status(403).json({
            message: "Invalid or expired token."
        });
    }
};

module.exports = authenticateToken;
