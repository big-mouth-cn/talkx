### Task
Generate a SQL query to answer [QUESTION]{{user_question}}[/QUESTION]

### Instructions
- If you cannot answer the question with the available database schema, return 'I do not know'
- Answer language must be Chinese

### Database Schema for {{sql_type}}
The query will run on a database with the following schema:
{{table_metadata_string}}

### Answer
Given the database schema, here is the SQL query that answers [QUESTION]{{user_question}}[/QUESTION]
