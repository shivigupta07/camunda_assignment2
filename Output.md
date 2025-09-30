- To start the process, send a message to the registration queue via JMS, and during the subprocess, send a message in the 'Send Inspection Request' step (JMS).
  
| Screenshot 1 | Screenshot 2 |
|--------------|--------------|
| <img width="770" height="424" alt="image" src="https://github.com/user-attachments/assets/094adfac-025d-4575-8068-8195472b69aa" /> | <img width="698" height="416" alt="image" src="https://github.com/user-attachments/assets/94fd9464-0e80-4e4b-b8b4-b13342b292fd" />
 |

# outputs

Example 1: Since the wage is missing, the flow first goes to the Admin task for 'Save and Update'. After the Admin updates the wage, it moves to the Validator task. Due to Round Robin assignment, the 'Request Clarification' step is triggered, sending it back to Admin for comment updates. Finally, it returns to the Validator task, and based on Round Robin assignment, the flow proceeds with a 'Reject' action.

<img width="429" height="283" alt="image" src="https://github.com/user-attachments/assets/0d5bf787-49bd-49b9-aaff-54af635c88dd" />


