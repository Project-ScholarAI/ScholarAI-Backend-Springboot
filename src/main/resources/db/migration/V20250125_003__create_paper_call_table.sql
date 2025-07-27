-- Create paper_call table
CREATE TABLE paper_call (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    link TEXT NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('conference', 'journal')),
    source VARCHAR(100) NOT NULL,
    domain VARCHAR(255) NOT NULL,
    when_info VARCHAR(255),
    where_info VARCHAR(255),
    deadline VARCHAR(255),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create indexes for filtering
CREATE INDEX idx_paper_call_user_id ON paper_call(user_id);
CREATE INDEX idx_paper_call_domain ON paper_call(domain);
CREATE INDEX idx_paper_call_type ON paper_call(type);
CREATE INDEX idx_paper_call_source ON paper_call(source);
CREATE INDEX idx_paper_call_created_at ON paper_call(created_at);

-- Add trigger for updated_at
CREATE TRIGGER trg_paper_call_updated
    BEFORE UPDATE ON paper_call
    FOR EACH ROW EXECUTE PROCEDURE set_updated_at(); 