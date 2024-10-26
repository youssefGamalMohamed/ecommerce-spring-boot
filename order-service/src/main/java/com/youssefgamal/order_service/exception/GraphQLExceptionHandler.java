package com.youssefgamal.order_service.exception;

import java.util.NoSuchElementException;

import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
    	log.error("Exception With Type = {}, Message = {}" , ex.getClass().getName().toString(), ex.getMessage());
        if (ex instanceof NoSuchElementException) {
            return GraphqlErrorBuilder.newError()
						              .errorType(ErrorType.NOT_FOUND)
						              .message(ex.getMessage())
						              .path(env.getExecutionStepInfo().getPath())
						              .location(env.getField().getSourceLocation())
						              .build();
        } 
        else {
        	
            return GraphqlErrorBuilder.newError()
		              .errorType(ErrorType.INTERNAL_ERROR)
		              .message(ex.getMessage())
		              .path(env.getExecutionStepInfo().getPath())
		              .location(env.getField().getSourceLocation())
		              .build();
        }
    }
}
