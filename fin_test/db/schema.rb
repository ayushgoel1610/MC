# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20141129080954) do

  create_table "api_keys", force: true do |t|
    t.string   "access_token"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "chatqueues", force: true do |t|
    t.integer  "user_id1"
    t.integer  "user_id2"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer  "topic_id"
    t.integer  "req_count"
    t.integer  "chat"
    t.integer  "locflag"
  end

  create_table "chats", force: true do |t|
    t.integer  "userid_1"
    t.integer  "userid_2"
    t.integer  "topic_id"
    t.integer  "reputation_1"
    t.integer  "reputation_2"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "recents", force: true do |t|
    t.string   "reputation"
    t.integer  "topic_id"
    t.integer  "chat_user_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer  "user_id"
  end

  create_table "topics", force: true do |t|
    t.string   "name"
    t.string   "category"
    t.integer  "user_count"
    t.integer  "health"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "user_topics", force: true do |t|
    t.integer  "topic_id"
    t.integer  "num_chats"
    t.integer  "avg_reputation"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer  "user_id"
  end

  create_table "users", force: true do |t|
    t.string   "name"
    t.string   "email"
    t.string   "password_digest"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "token"
    t.integer  "reputation"
    t.integer  "chat_id"
    t.string   "location"
  end

  add_index "users", ["email"], name: "index_users_on_email", unique: true, using: :btree

end
