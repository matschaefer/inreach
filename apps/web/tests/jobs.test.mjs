import test from 'node:test';
import assert from 'node:assert/strict';
import {cleanPage,cleanQuery,safeJobUrl} from '../src/lib/jobs.ts';
test('search inputs are bounded',()=>{assert.equal(cleanPage('-1'),1);assert.equal(cleanPage('2'),2);assert.equal(cleanPage('1.5'),1);assert.equal(cleanQuery(' x '),'x');assert.equal(cleanQuery('x'.repeat(121)).length,120)});
test('unsafe outbound links are rejected',()=>{assert.equal(safeJobUrl('javascript:alert(1)'),null);assert.equal(safeJobUrl('not a URL'),null);assert.equal(safeJobUrl('https://example.com/job'),'https://example.com/job')});
