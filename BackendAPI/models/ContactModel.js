
const { getPool, sql } = require('../config/db');

const ContactModel = {
    // Get all contact enquiries
    async getAll() {
        const pool = await getPool();

        const result = await pool.request().query(`
            SELECT
                Id AS id,
                FullName AS name,
                Email AS email,
                PhoneNumber AS phone,
                Subject AS subject,
                Message AS message,
                SubmittedAt AS createdAt,
                UserId AS userId
            FROM dbo.ContactEnquiries
            ORDER BY SubmittedAt DESC;
        `);

        return result.recordset;
    },

    // Get contact enquiry by ID
    async getById(id) {
        const pool = await getPool();

        const result = await pool.request()
            .input('id', sql.Int, Number(id))
            .query(`
                SELECT
                    Id AS id,
                    FullName AS name,
                    Email AS email,
                    PhoneNumber AS phone,
                    Subject AS subject,
                    Message AS message,
                    SubmittedAt AS createdAt,
                    UserId AS userId
                FROM dbo.ContactEnquiries
                WHERE Id = @id;
            `);

        return result.recordset[0] || null;
    },

    // Create contact enquiry
    async create(contactData) {
        const pool = await getPool();

        const result = await pool.request()
            .input('name', sql.NVarChar(150), contactData.name)
            .input('email', sql.NVarChar(150), contactData.email)
            .input('phone', sql.NVarChar(30), contactData.phone || null)
            .input('subject', sql.NVarChar(200), contactData.subject)
            .input('message', sql.NVarChar(3000), contactData.message)
            .input('userId', sql.Int, contactData.userId ?? null)
            .query(`
                INSERT INTO dbo.ContactEnquiries
                    (FullName, Email, PhoneNumber, Subject, Message,
                     SubmittedAt, UserId)
                OUTPUT
                    INSERTED.Id AS id,
                    INSERTED.FullName AS name,
                    INSERTED.Email AS email,
                    INSERTED.PhoneNumber AS phone,
                    INSERTED.Subject AS subject,
                    INSERTED.Message AS message,
                    INSERTED.SubmittedAt AS createdAt,
                    INSERTED.UserId AS userId
                VALUES
                    (@name, @email, @phone, @subject, @message,
                     SYSUTCDATETIME(), @userId);
            `);

        return result.recordset[0];
    }
};

module.exports = ContactModel;
