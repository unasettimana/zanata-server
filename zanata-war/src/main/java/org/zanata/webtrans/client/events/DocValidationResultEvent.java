package org.zanata.webtrans.client.events;

import java.util.Date;
import java.util.Set;

import org.zanata.webtrans.shared.model.DocumentId;

import com.google.gwt.event.shared.GwtEvent;

public class DocValidationResultEvent extends GwtEvent<DocValidationResultHandler>
{
   private Date startTime;
   private Date endTime;
   private Set<DocumentId> errorDocs;

   public DocValidationResultEvent(Date startTime, Date endTime, Set<DocumentId> errorDocs)
   {
      this.startTime = startTime;
      this.endTime = endTime;
      this.errorDocs = errorDocs;
   }

   /**
    * Handler type.
    */
   private static Type<DocValidationResultHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<DocValidationResultHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<DocValidationResultHandler>();
      }
      return TYPE;
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<DocValidationResultHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   protected void dispatch(DocValidationResultHandler handler)
   {
      handler.onCompleteRunDocValidation(this);
   }

   public Date getStartTime()
   {
      return startTime;
   }

   public Date getEndTime()
   {
      return endTime;
   }

   public Set<DocumentId> getErrorDocs()
   {
      return errorDocs;
   }
}