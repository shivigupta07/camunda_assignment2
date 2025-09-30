- To start the process, send a message to the registration queue via JMS, and during the subprocess, send a message in the 'Send Inspection Request' step (JMS).
  
| Screenshot 1 | Screenshot 2 |
|--------------|--------------|
| <img width="770" height="424" alt="image" src="https://github.com/user-attachments/assets/094adfac-025d-4575-8068-8195472b69aa" /> | <img width="698" height="416" alt="image" src="https://github.com/user-attachments/assets/94fd9464-0e80-4e4b-b8b4-b13342b292fd" />
 |

# outputs

Example 1: Since the wage is missing, the flow first goes to the Admin task for 'Save and Update'. After the Admin updates the wage, it moves to the Validator task. Due to Round Robin assignment, the 'Request Clarification' step is triggered, sending it back to Admin for comment updates. Finally, it returns to the Validator task, and based on Round Robin assignment, the flow proceeds with a 'Reject' action.

| Screenshot 1 | Screenshot 2 |
|--------------|--------------|
| <img width="429" height="283" alt="image" src="https://github.com/user-attachments/assets/0d5bf787-49bd-49b9-aaff-54af635c88dd" /> | <img width="914" height="287" alt="image" src="https://github.com/user-attachments/assets/a4fc6b4e-4bb1-458f-bea0-1ee6b4076813" /> |

Example 2: The flow starts, at the Admin task, where the wage is updated. It then moves to the Validator task, which, through Round Robin assignment, triggers the 'Raise Inspection' subprocess. Within this subprocess, an inspection request is sent via JMS. Different users are assigned tasks based on Round Robin—Legal performs the legal review, Finance performs the financial review, and the flow continues. The assignments and final decisions can be tracked through variables. Finally, the process returns to the Validator task, which, again using Round Robin, proceeds along the 'Reject' path.

| Screenshot 1 | Screenshot 2 | Screenshot 3 |
|--------------|--------------|--------------|
| <img width="400" height="269" alt="image" src="https://github.com/user-attachments/assets/7737b08d-a48d-43b8-a424-72f9f69ff444" /> | <img width="802" height="221" alt="image" src="https://github.com/user-attachments/assets/df04ea70-f123-41c0-a3d2-252efa6fc09f" /> | <img width="709" height="247" alt="image" src="https://github.com/user-attachments/assets/1d0eb07d-f857-455b-9790-1b6870159459" />
 |

Example 3: Since the wage is already provided, the process moves directly to the Validator task. Through Round Robin, the 'Request Clarification' path is triggered, sending it back to Admin for clarification. Afterward, it returns to the Validator task, where Round Robin assigns it to the 'Approve' path. The gateway checks if the request is backdated; since it is not, the flow proceeds without any further routing and calls the Activate Registration API, completing the process.

| Screenshot 1 | Screenshot 2 |
|--------------|--------------|
| <img width="410" height="278" alt="image" src="https://github.com/user-attachments/assets/c58d7f01-95bd-4405-9bcf-2da3c8bd5016" /> | <img width="673" height="246"  alt="image" src="https://github.com/user-attachments/assets/4811747c-7d3a-4b72-9b0d-6b13a1ea7c6b" /> |





