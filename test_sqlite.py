import sqlite3

conn = sqlite3.connect(':memory:')
cursor = conn.cursor()
cursor.execute('''
    CREATE TABLE IF NOT EXISTS config (
        guild_id INTEGER, key TEXT, value TEXT,
        PRIMARY KEY (guild_id, key)
    )
''')
cursor.execute("INSERT INTO config VALUES (123, 'grade', '1')")
cursor.execute("INSERT INTO config VALUES (123, 'class_nm', '3')")
conn.commit()

# Test the query that we used
grade = "1"
class_nm = "3"
cursor.execute("SELECT guild_id FROM config WHERE key='grade' AND value=?", (str(grade),))
grade_guilds = set([row[0] for row in cursor.fetchall()])

cursor.execute("SELECT guild_id FROM config WHERE key='class_nm' AND value=?", (str(class_nm),))
class_guilds = set([row[0] for row in cursor.fetchall()])

common = grade_guilds.intersection(class_guilds)
print("Guilds:", common)
