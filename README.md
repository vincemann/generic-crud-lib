This is a Spring boot library, aiming to make the repetitive task, of implementing crud operations for all of your entities, become a cinch.  
Therefore some extendable powerful generic and astract classes are provided for doing all the work, that comes with implementing crud.  
Features:  
Crud-Service Layer  
Crud-Controller Layer  
Abstract Integration-Test for the Crud-Controller Layer  
Abstract Test for Crud-Service Layer  
validation Layer
exception Handling for Crud-Controller-Layer  
support for Bidirectional Entity Relationships (i.e. @OneToMany, @ManyToOne)  
     -> automatically manages both sides of the bidirectional relationship (i.e. setting of backrefences) for all crud                                     operations  
     DTO-to-ServiceEntity-mapping layer (and vice versa), integrated in the Crud-Controller-Layer  
  
Checkout the example application for usageinfos and deatils
