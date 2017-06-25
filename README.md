#Shutterfly Customer Lifetime Value

One way to analyze acquisition strategy and estimate marketing cost is to calculate the Lifetime Value (“LTV”) of a customer. Simply speaking, LTV is the projected revenue that customer will generate during their lifetime.

A simple LTV can be calculated using the following equation: 52(a) x t. Where a is the average customer value per week (customer expenditures per visit (USD) x number of site visits per week) and t is the average customer lifespan. The average lifespan for Shutterfly is 10 years.

## Code Requirements

Write a program that ingests event data and implements one analytic method, below. You are expected to write clean, well-documented and well-tested code. Be sure to think about performance - what the performance characteristic of the code is and how it could be improved in the future.

You may use one of the following OO languages: Java, Python, Scala.

### Ingest(e, D)
Given event e, update data D

### TopXSimpleLTVCustomers(x, D)
Return the top x customers with the highest Simple Lifetime Value from data D. 

#Assumptions:
- All customers are assumed to be active during the period from their first order till the date last order is received.
- Input files should be of .txt format available in the input folder.
- Ingest function consumes single event every time it is called.
- A NEW event may appear in any order. and an UPDATE has the same key as a NEW event.
- It is assumed that SITE_VISIT will be definitely received for all customers who visited the site.
- Usually a NEW event is received once and UPDATE can be received many times for every type of event but if due to some issue we receive another NEW event with the same key then the event which has a latest timestamp will be saved in the main data structure while the event with old timestamp will be pushed as backup event.
- When an UPDATE event is received, the respective elements in the old event is updated. If no old event is found then the event is directly pushed. If, however the UPDATE event received has a timestamp of an earlier date in the event_time, the event is not pushed, rather it's stored as a backup event.

- It is assumed that only one event having a old timestamp will saved as a backup event when a new event replaces it in the main data structure. In case, old backup event is already present and another event is pushed to be backed up then it will replace the old event which is backed up.

- It is assumed that the number of weeks will be calculated based on the individual orders. A customer is active for the weeks in which he has placed an order.

# Approach
- As assumed, number of weeks is counted based on the number of weeks when orders were placed. A week start date was considered as the date of the first day (Sunday) of that week.

- The total expenditure is cumulative expenses any individual customer has spent while placing an order. If any existing order is updated, only the difference amount is added or subtracted on the total expenditure.

- The LTV of a customer is calculated in the topXSimpleLTVCustomers() method. Here I have considered that a customer is active with from the date of the customer's first order is placed till the week the last order is received from any customer. Finally the LTV values of top X customers is filtered and stored in the output.txt in the output folder.

- So, if the customer doesn't have orders in any of those weeks in that duration, the order value is assumed as zero. This is done to satisfy the data condition as given in the FAQs.

- As an alternate solution, I have also designed a method (topXSimpleLTVCustomersAlt) to calculate the number of weeks a customer differently. Here we take into account the count of weeks and total expenditure of a customer to calculate the Lifetime Customer Value for 10 years.

- A customer whose order information is not available is not considered active, hence not taken into account during LTV calculation.

- I have designed an addon feature of persistence of data. Persistence of data can be enabled or disabled from the SflyApp (main class) by toggling the enableSerialization boolean variable. When persistence is enabled the event data will be serialized and saved as "persistDB.dat" in the output folder. While running the application again the application looks for the "persistDB.dat" file and if it finds it will read the data and fill in the data structure before ingesting any new events. 


