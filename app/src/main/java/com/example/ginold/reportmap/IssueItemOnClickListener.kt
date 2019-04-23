package com.example.ginold.reportmap

import com.example.ginold.reportmap.models.Issue

/**
 * Created by ginold on 15.12.2017.
 */

interface IssueItemOnClickListener {
    fun onIssueItemClick(pos: Int, issue: Issue)
}