# Registration Process: 
Demonstrates a BPM-based process with dynamic task 
assignment, API integration, message correlation, and conditional routing. Supports 
multi-role collaboration with real-time SLA, audit, and comment tracking
- Process initiated by Message (JMS) or message start event – Feel free to design the 
input request based on the data elements needed for the process
If the request has an “Missing Wage” then assign to “Admin” group pool 
(Group members can claim and act).
Action can perform: “Save & Update”.
If Admin does not complete this task within 7 days, Then call the activate 
API( Create a mock API ) with indicator default.
- Assign the process to “Validator Group” (Round Robin Assignment).
Actions Validator can perform: “Request Clarification”, “Reject”, 
“Approve”, “Raise Inspection”.
For all case User should able to add “Comments / Remarks” with the ability 
to mark “Public Comment” or “Private Comment”.
If request Clarification, Assign the task to “Admin” pool (Group members 
can claim and act).
- If the request has an indicator “Backdated” then assign to “Financial Controller 
Group” (Round Robin Assignment).
Action can perform: “Approve”, “Return”.
If “Return” selected assign to same validator who has worked in the 
previous step.
- Call Activate registration API if all are approved OR end the process.
API secured with OAuth client credentials
REST API with POST/PATCH
# Raise Inspection Sub process:
- Send Message a to Inspection system and wait for the response (May take day to 
get the feedback)
- Inspection system will give response back to JMS queue, Corelate the message 
and move to next step
- If the response contain “Violation” indicator true, Assign to “Violation committee” 
with following condition met.
- There are two violation committee, “C1” and “C2”. Task should be assigned round 
Robbin fashion between this groups.
- Each group has “Legal”, “Finance” and “Head” subgroups with multiple members 
in each. Once the committee selected within that committee, task has to be 
assigned parallelly to “Legal” and “Finance”
Action performed: “Approve”
User can add “Comment” and “Decision” dropdown with options “Class 
1”, “Class 2”, “Class 3”
If both parallel users selected same “Decision” then go to #5
If both are different assign to “Head” member of same committee.
- If Approved assign task to #3 of same validator (Main process)
# Generic Requirements:
Worklist population API
- Task details
- Valid Action user can perform
- Previous Comments with user who has entered
- Task audit (previous users who have acted and the action)
- SLA / OLA information
Event to publish for all user task status changes
- Assigned
- Action performed (Approve, reject, Return, Raise inspection etc…)
- Re-assigned
- Cancelled
SLA / OLA tracking of human task
Monitoring / analyzing process
